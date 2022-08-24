package io.chain.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.starkbank.ellipticcurve.Ecdsa;
import io.chain.models.exceptions.*;
import io.chain.models.serialization.TransactionDeserializer;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.Shareable;
import lombok.ToString;
import lombok.Value;
import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static io.chain.models.Wallet.COINBASE;

@Value
@ToString
@JsonDeserialize(using = TransactionDeserializer.class)
public class Transaction implements Hashable, Shareable {

    List<Input> inputs;
    List<Output> outputs;
    byte[] data;

    public Transaction(List<Input> inputs, List<Output> outputs) {
        this(inputs, outputs, null);
    }

    public Transaction(List<Input> inputs, List<Output> outputs, byte[] data) {
        this.inputs = new ArrayList<>(inputs);
        this.outputs = new ArrayList<>(outputs);
        this.data = data;
    }

    public JsonObject toJson() {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonObject result = new JsonObject();

        try {
            result.put("txHash", hash());
            result.put("inputs", Buffer.buffer(mapper.writeValueAsString(inputs)).toJsonArray());
            result.put("outputs", Buffer.buffer(mapper.writeValueAsString(outputs)).toJsonArray());
            if (data != null) {
                result.put("data", Buffer.buffer(mapper.writeValueAsString(data)).toString());
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return result;
    }

    @JsonIgnore
    public boolean isValidRewardTx() {
        if (getInputs().size() != 1) return false;
        if ( ! new String(getInputs().get(0).getTxHash(), StandardCharsets.UTF_8).equals("COINBASE")) return false;
        if (getInputs().get(0).getIndex() != 0) return false;
        final String verifiableData = Buffer.buffer(getRawDataToSign(0)).toString();
        if ( ! Ecdsa.verify(verifiableData, getInputs().get(0).getSignature(), COINBASE.getPk())) return false;

        if (getOutputs().size() != 1) return false;
        return getOutputs().get(0).getAmount() != Block.MINING_REWARD;
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

            final byte[] address = out.getAddress().toByteString().getBytes();
            for (byte b : address) signData.add(b);
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

            /*
             * NOTE:
             * By including input signatures we ensure that the signed unconfirmed pool of transactions
             * will not replace multiple transactions to the same recipient with the same amount. This is intentional
             * behavior.
             */
            if (in.getSignature() != null) {
                final byte[] base64Sig = in.getSignature().toBase64().getBytes();
                for (byte b : base64Sig) rawTx.add(b);
            }
        }

        for (Output out: outputs) {
            final Buffer amountBuffer = Buffer
                                        .buffer(Integer.SIZE / 8)
                                        .setInt(0, out.getAmount());
            for (int i = 0; i < amountBuffer.length(); i++)
                rawTx.add(amountBuffer.getByte(i));

            final byte[] outAddress = out.getAddress().toByteString().getBytes();
            for (byte address : outAddress) rawTx.add(address);
        }

        if (data != null)
            for (byte datum : data) rawTx.add(datum);

        byte[] result = new byte[rawTx.size()];
        for (int i = 0; i < rawTx.size(); i++)
            result[i] = rawTx.get(i);
        return result;
    }

    /*
     * Static
     */

    public static Transaction rewardTransaction(Wallet minerWallet) {
        System.out.println("COINBASE PK: " + Hex.toHexString(COINBASE.getPk().toByteString().getBytes()));
        return COINBASE.sign(new Transaction(
            List.of(new Input("COINBASE".getBytes(StandardCharsets.UTF_8), 0)),
            List.of(new Output(minerWallet.getPk(), Block.MINING_REWARD))
        ));
    }

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

            final UTxO utxo;
            if (tx.isValidRewardTx()) {
                utxo = new UTxO(new Input(tx.hash().getBytes(), 0), tx.getOutputs().get(0));
            } else {
                if ( ! currentUtxoSet.contains(input)) // prevent double spending
                    throw new DoubleSpendException(input, tx);
                utxo = currentUtxoSet.get(input);
                if (input.getSignature() == null)
                    throw new MissingSignatureException(utxo.getTxOut().getAddress(), tx);
                final String verifiableData = Buffer.buffer(tx.getRawDataToSign(i)).toString();
                if ( ! Ecdsa.verify(verifiableData, input.getSignature(), utxo.getTxOut().getAddress()))
                    throw new MissingSignatureException(utxo.getTxOut().getAddress(), tx);
            }

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
