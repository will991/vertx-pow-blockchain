package io.chain.verticles;

import io.chain.models.Blockchain;
import io.chain.models.UTxOSet;
import io.chain.p2p.handlers.BlockchainSyncHandler;
import io.vertx.core.json.JsonArray;
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
        register(NEW_BLOCKCHAIN, msg ->
            new BlockchainSyncHandler(uuid, blockchain).handle(((JsonArray) msg.body()).toBuffer())
        );
    }
}
