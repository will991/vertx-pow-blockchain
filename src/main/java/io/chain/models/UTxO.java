package io.chain.models;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("input")
    Input txIn;
    @JsonProperty("output")
    Output txOut;

    @Override
    public int compareTo(UTxO o) {
        return Integer.compare(txOut.getAmount(), o.getTxOut().getAmount());
    }
}
