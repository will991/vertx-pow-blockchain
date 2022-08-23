package io.chain.models;

import com.starkbank.ellipticcurve.Ecdsa;
import com.starkbank.ellipticcurve.PrivateKey;
import com.starkbank.ellipticcurve.PublicKey;
import com.starkbank.ellipticcurve.Signature;
import io.chain.models.exceptions.InsufficientUTxOBalanceException;
import io.vertx.core.buffer.Buffer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import org.bouncycastle.util.encoders.Hex;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.lang.System.lineSeparator;
import static java.util.Collections.unmodifiableList;

@ToString
public final class Wallet {
    public static final Wallet COINBASE = new Wallet();

    private final List<UTxO> utxos;
    @Getter(AccessLevel.PRIVATE)
    private final PrivateKey sk;
    @Getter
    private final PublicKey pk;

    public Wallet () {
        this.utxos = new ArrayList<>();
        this.sk = new PrivateKey();
        this.pk = sk.publicKey();
    }

    public Wallet(PrivateKey sk, PublicKey pk, UTxO... utxos) throws MismatchingUTxOAddressException {
        this(sk, pk, new ArrayList<>(Arrays.asList(utxos)));
    }

    public Wallet (PrivateKey sk, PublicKey pk, List<UTxO> utxos) throws MismatchingUTxOAddressException {
        this.sk = sk;
        this.pk = pk;

        final Optional<UTxO> utxo = utxos.stream()
                .filter(u -> ! u.getTxOut().getAddress().equals(this.pk))
                .findFirst();
        if (utxo.isPresent()) {
            throw new MismatchingUTxOAddressException(this.pk, utxo.get());
        }
        Collections.sort(utxos, Collections.reverseOrder());
        this.utxos = utxos;
    }

    public void setUTxOSet(Set<UTxO> newUTxOSet) throws MismatchingUTxOAddressException {
        utxos.clear();
        for (UTxO utxo : newUTxOSet) {
            addUTxO(utxo);
        }
    }

    public List<UTxO> getUTxOs() {
        return unmodifiableList(utxos);
    }

    public void addUTxO(final UTxO utxo) throws MismatchingUTxOAddressException {
        if ( ! utxo.getTxOut().getAddress().equals(pk)) {
            throw new MismatchingUTxOAddressException(pk, utxo);
        }

        if (utxos.isEmpty()) {
            utxos.add(utxo);
            return;
        }

        for (int i = 0; i < utxos.size(); i++) {
            final UTxO u = utxos.get(i);
            if (u.getTxOut().getAmount() < utxo.getTxOut().getAmount()) {
                utxos.add(i, utxo);
                break;
            } else if (i == utxos.size() - 1) {
                utxos.add(utxo);
                break;
            }
        }
    }

    public Transaction sign(Transaction tx) {
        for (int i = 0; i < tx.getInputs().size(); i++) {
            final String signableData = Buffer.buffer(tx.getRawDataToSign(i)).toString();
            final Signature signature = Ecdsa.sign(signableData, sk);
            tx.getInputs().get(i).addSignature(signature);
        }
        return tx;
    }

    public Transaction create(PublicKey recipient, int amount) throws InsufficientUTxOBalanceException {
        return create(recipient, amount, null);
    }

    public Transaction create(PublicKey recipient, int amount, byte[] data) throws InsufficientUTxOBalanceException {
        final List<UTxO> availableUTxOs = new ArrayList<>(getUTxOs());
        final List<UTxO> selectedUTxOs = new ArrayList<>();
        final Function<List<UTxO>, Integer> calculateBalance = us ->
                us.parallelStream()
                    .reduce(0,
                        (acc, utxo) -> acc + utxo.getTxOut().getAmount(),
                        Integer::sum
                    );

        do {
            if (availableUTxOs.isEmpty()) {
                throw new InsufficientUTxOBalanceException(amount, balance());
            }
            selectedUTxOs.add(availableUTxOs.remove(0));
        } while (calculateBalance.apply(selectedUTxOs) < amount);

        final List<Input> txInputs = selectedUTxOs.stream().map(UTxO::getTxIn).collect(Collectors.toList());
        final List<Output> txOutputs = new ArrayList<>(
            Arrays.asList(
                new Output(recipient, amount)
            )
        );
        final int change = calculateBalance.apply(selectedUTxOs) - amount;
        if (change > 0) {
            txOutputs.add(new Output(pk, change));
        }
        return new Transaction(txInputs, txOutputs, data);
    }

    public int balance() {
        return utxos
                .parallelStream()
                .reduce(0,
                    (acc, in) -> acc + in.getTxOut().getAmount(),
                    Integer::sum
                );
    }

    /*
     * Wallet Exceptions
     */

    public static final class MismatchingUTxOAddressException extends Exception {

        public MismatchingUTxOAddressException(PublicKey pk, UTxO utxo) {
            super(
                format(
                    "UTxO does not belong to Wallet. Mismatching wallet public key.%sExpected: %s%sActual: %s",
                    lineSeparator(),
                    Hex.toHexString(pk.toByteString().getBytes()),
                    lineSeparator(),
                    Hex.toHexString(utxo.getTxOut().getAddress().toByteString().getBytes())
                )
            );
        }
    }
}
