package io.chain.p2p;

import lombok.Getter;

@Getter
public enum EventBusAddresses {
    NEW_BLOCKCHAIN("blockchain.new");

    private final String address;

    EventBusAddresses(String suffix) {
        this.address = String.format("io.chain.%s", suffix);
    }
}
