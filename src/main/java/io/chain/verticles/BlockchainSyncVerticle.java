package io.chain.verticles;

import io.chain.models.Blockchain;
import io.chain.models.UTxOSet;
import io.chain.p2p.handlers.BlockchainSyncHandler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static io.chain.p2p.EventBusAddresses.NEW_BLOCKCHAIN;

@Getter
@RequiredArgsConstructor
public final class BlockchainSyncVerticle extends AbstractEventBusVerticle {

    private final Blockchain blockchain;
    private final UTxOSet utxos;
    private final String uuid;

    @Override
    public void start() {
        register(NEW_BLOCKCHAIN, new BlockchainSyncHandler(uuid, blockchain));
    }
}
