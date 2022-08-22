package io.chain.models;

import com.starkbank.ellipticcurve.Ecdsa;
import com.starkbank.ellipticcurve.PublicKey;
import com.starkbank.ellipticcurve.Signature;
import io.chain.models.exceptions.*;
import io.vertx.core.buffer.Buffer;
import lombok.ToString;
import lombok.Value;
import org.apache.commons.codec.digest.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Value
@ToString
public class Transaction implements Hashable {

    private final List<Input> inputs;
    private final List<Output> outputs;
    byte[] data;

    public Transaction(List<Input> ins, List<Output> outs, byte[] data) {
        this.inputs = new ArrayList<>(ins);
        this.outputs = new ArrayList<>(outs);
        this.data = data;
    }

    public boolean addInput(String txHash, int outIdx) {
        return inputs.add(new Input(txHash.getBytes(StandardCharsets.UTF_8), outIdx));
    }

    public boolean addOutput(int amount, PublicKey address) {
        return outputs.add(new Output(address, amount));
    }

    public void addSignature(Signature signature, int idx) {
        inputs.get(idx).addSignature(signature);
    }

    @Override
    public String hash() {
        return Transaction.hash(this);
    }

    public byte[] getRawDataToSign(int index) {
        final ArrayList<Byte> signData = new ArrayList<>();
        if (index < 0 || index > inputs.size()) return null;
        final Input in = inputs.get(index);
        if (in.getTxHash() != null)
            for (int i = 0; i < in.getTxHash().length; i++)
                signData.add(in.getTxHash()[i]);

        final Buffer inIdxBuffer = Buffer
                .buffer(Integer.SIZE / 8)
                .setInt(0, in.getIndex());
        for (int i = 0; i < inIdxBuffer.length(); i++)
            signData.add(inIdxBuffer.getByte(i));

        for (Output out: outputs) {
            final Buffer amountBuffer = Buffer
                    .buffer(Integer.SIZE / 8)
                    .setInt(0, out.getAmount());
            for (int i = 0; i < amountBuffer.length(); i++)
                signData.add(amountBuffer.getByte(i));

            final byte[] address = out.getAddress().toByteString(true).getBytes();
            for (int i = 0; i < address.length; i++)
                signData.add(address[i]);
        }
        byte[] result = new byte[signData.size()];
        for (int i = 0; i < signData.size(); i++)
            result[i] = signData.get(i);
        return result;
    }

    public byte[] toRaw() {
        final ArrayList<Byte> rawTx = new ArrayList<>();
        for (Input in: inputs) {
            if (in.getTxHash() != null)
                for (int i = 0; i < in.getTxHash().length; i++)
                    rawTx.add(in.getTxHash()[i]);

            final Buffer inIdxBuffer = Buffer
                                        .buffer(Integer.SIZE / 8)
                                        .setInt(0, in.getIndex());
            for (int i = 0; i < inIdxBuffer.length(); i++)
                rawTx.add(inIdxBuffer.getByte(i));
            if (in.getSignature() != null) {
                final byte[] base64Sig = in.getSignature().toBase64().getBytes();
                for (int i = 0; i < base64Sig.length; i++)
                    rawTx.add(base64Sig[i]);
            }
        }

        for (Output out: outputs) {
            final Buffer amountBuffer = Buffer
                                        .buffer(Integer.SIZE / 8)
                                        .setInt(0, out.getAmount());
            for (int i = 0; i < amountBuffer.length(); i++)
                rawTx.add(amountBuffer.getByte(i));

            final byte[] outAddress = out.getAddress().toByteString(true).getBytes();
            for (int i = 0; i < outAddress.length; i++)
                rawTx.add(outAddress[i]);
        }

        if (data != null)
            for (int i = 0; i < data.length; i++)
                rawTx.add(data[i]);

        byte[] result = new byte[rawTx.size()];
        for (int i = 0; i < rawTx.size(); i++)
            result[i] = rawTx.get(i);
        return result;
    }

    /*
     * Static
     */

    public static boolean isValid(Transaction tx, UTxOSet currentUtxoSet) {
        try {
            validate(tx, currentUtxoSet);
            return true;
        } catch (TransactionValidationException e) {
            return false;
        }
    }

    public static void validate(Transaction tx, UTxOSet currentUtxoSet) throws TransactionValidationException {
        final UTxOSet newUtxoSet = new UTxOSet();
        int inSum = 0;
        int outSum = 0;

        for (int i = 0; i < tx.getInputs().size(); i++) {
            final Input input = tx.getInputs().get(i);
            if ( ! currentUtxoSet.contains(input)) // prevent double spending
                throw new DoubleSpendException(input, tx);
            final UTxO utxo = currentUtxoSet.get(input);
            final String verifiableData = Buffer.buffer(tx.getRawDataToSign(i)).toString(StandardCharsets.UTF_8);
            if (input.getSignature() == null)
                throw new MissingSignatureException(utxo.getTxOut().getAddress(), tx);
            if ( ! Ecdsa.verify(verifiableData, input.getSignature(), utxo.getTxOut().getAddress()))
                throw new MissingSignatureException(utxo.getTxOut().getAddress(), tx);
            if (newUtxoSet.contains(input))
                throw new DoubleSpendException(input, tx); // utxo consumed multiple times in same tx
            newUtxoSet.add(utxo);
            inSum += utxo.getTxOut().getAmount();
        }

        for (Output out: tx.getOutputs()) {
            if (out.getAmount() <= 0)
                throw new InvalidOutputValueException(out, tx);  // check no negative output amounts
            outSum += out.getAmount();
        }

        if (inSum < outSum)
            throw new UnbalancedTransactionException(inSum, outSum, tx);
    }

    public static String hash(Transaction tx) {
        return DigestUtils.sha3_256Hex(tx.toRaw());
    }
}
