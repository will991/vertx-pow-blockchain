package io.chain.models;

import com.starkbank.ellipticcurve.Ecdsa;
import com.starkbank.ellipticcurve.PrivateKey;
import com.starkbank.ellipticcurve.PublicKey;
import com.starkbank.ellipticcurve.Signature;
import io.vertx.core.buffer.Buffer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import org.bouncycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static java.lang.System.lineSeparator;

@Getter
@ToString
public final class Wallet {

    private final List<UTxO> utxos;
    @Getter(AccessLevel.PRIVATE)
    private final PrivateKey sk;
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
        this.utxos = utxos;
    }

    public void addUTxO(final UTxO utxo) throws MismatchingUTxOAddressException {
        if ( ! utxo.getTxOut().getAddress().equals(pk)) {
            throw new MismatchingUTxOAddressException(pk, utxo);
        }
        utxos.add(utxo);
    }

    public Transaction sign(Transaction tx) {
        for (int i = 0; i < tx.getInputs().size(); i++) {
            final String signableData = Buffer.buffer(tx.getRawDataToSign(i)).toString(StandardCharsets.UTF_8);
            final Signature signature = Ecdsa.sign(signableData, sk);
            tx.getInputs().get(i).addSignature(signature);
        }
        return tx;
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
                    pk.toByteString(true),
                    lineSeparator(),
                    utxo.getTxOut().getAddress().toByteString(true)
                )
            );
        }
    }
}
