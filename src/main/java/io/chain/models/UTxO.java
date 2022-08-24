package io.chain.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.chain.models.serialization.UTxODeserializer;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.Value;

@Value
@ToString
@RequiredArgsConstructor
@JsonDeserialize(using = UTxODeserializer.class)
public class UTxO implements Comparable<UTxO> {
    Input txIn;
    Output txOut;

    @Override
    public int compareTo(UTxO o) {
        return Integer.compare(txOut.getAmount(), o.getTxOut().getAmount());
    }
}
