package io.chain.models;


import com.starkbank.ellipticcurve.PublicKey;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Objects;

@Getter
@ToString
@RequiredArgsConstructor
public final class Output {
    private final PublicKey address;
    private final int amount;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Output output = (Output) o;
        return amount == output.amount && Objects.equals(address, output.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, amount);
    }
}
