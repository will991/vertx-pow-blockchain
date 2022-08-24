package io.chain.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.starkbank.ellipticcurve.Signature;
import io.chain.models.serialization.ByteArrayHexDeserializer;
import io.chain.models.serialization.ByteArrayHexSerializer;
import io.chain.models.serialization.InputDeserializer;
import io.chain.models.serialization.SignatureHexSerializer;
import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;
import java.util.Objects;

@Getter
@ToString
@JsonDeserialize(using = InputDeserializer.class)
public final class Input implements Comparable<Input> {
    @JsonSerialize(using = ByteArrayHexSerializer.class)
    @JsonDeserialize(using = ByteArrayHexDeserializer.class)
    private final byte[] txHash;
    private final int index;
    @JsonSerialize(using = SignatureHexSerializer.class)
    private Signature signature;

    public Input(byte[] prevTxHash, int index) {
        this(prevTxHash, index, null);
    }

    public Input(byte[] prevTxHash, int index, Signature signature) {
        txHash = prevTxHash == null ? null : Arrays.copyOf(prevTxHash, prevTxHash.length);
        this.index = index;
        this.signature = signature;
    }

    public void addSignature(Signature sig) {
        signature = sig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Input input = (Input) o;

        /*
         * NOTE: By excluding the signature of the equals method and hashcode, we ensure that
         * the signed unconfirmed pool of transactions can still map inputs to UTxOs. Otherwise, the pool would not be
         * able to distinguish between available/ spendable UTxOs and a signed version of those.
         */
        return index == input.index && Arrays.equals(txHash, input.txHash);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(index);
        result = 31 * result + Arrays.hashCode(txHash);
        /*
         * NOTE: Read note from equals method, why signature is excluded from hashcode.
         */
        return result;
    }

    @Override
    public int compareTo(Input o) {
        if (o.getIndex() > index) return -1;
        if (o.getIndex() < index) return 1;
        if (o.getTxHash().length > txHash.length) return -1;
        if (o.getTxHash().length < txHash.length) return 1;
        for (int i = 0; i < txHash.length; i++) {
            if (o.getTxHash()[i] > txHash[i]) return -1;
            if (o.getTxHash()[i] < txHash[i]) return 1;
        }
        return 0;
    }
}
