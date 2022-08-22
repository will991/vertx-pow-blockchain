package io.chain.models;

import com.starkbank.ellipticcurve.Signature;
import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;
import java.util.Objects;

@Getter
@ToString
public final class Input implements Comparable<Input> {
    private final byte[] txHash;
    private final int index;
    private Signature signature = null;

    public Input(byte[] prevTxHash, int index) {
        txHash = prevTxHash == null ? null : Arrays.copyOf(prevTxHash, prevTxHash.length);
        this.index = index;
    }

    public void addSignature(Signature sig) {
        signature = sig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Input input = (Input) o;
        if (
            signature == null && input.signature != null ||
            signature != null && input.signature == null
        )
            return false;

        return index == input.index
            && Arrays.equals(txHash, input.txHash)
            && (
                signature == null && input.signature == null ||
                signature.toBase64().equals(input.signature.toBase64())
            );
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(index);
        result = 31 * result + Arrays.hashCode(txHash);
        if (signature != null)
            result = 31 * result + signature.toBase64().hashCode();
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
