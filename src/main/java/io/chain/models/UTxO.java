package io.chain.models;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.Value;

@Value
@ToString
@RequiredArgsConstructor
public class UTxO {
    Input txIn;
    Output txOut;
}
