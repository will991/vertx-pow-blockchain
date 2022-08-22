package io.chain.verticles;

import io.chain.models.Blockchain;
import io.chain.models.UTxOSet;
import io.chain.p2p.handlers.BlockchainSyncHandler;
import io.vertx.core.json.JsonArray;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import static io.chain.p2p.EventBusAddresses.NEW_BLOCKCHAIN;

@Getter
@RequiredArgsConstructor
public final class BlockchainSyncVerticle extends AbstractEventBusVerticle {

    private final Blockchain blockchain;
    private final UTxOSet utxos;
    private final String uuid = UUID.randomUUID().toString();

    @Override
    public void start() {
        register(NEW_BLOCKCHAIN, msg ->
            new BlockchainSyncHandler(uuid, blockchain).handle(((JsonArray) msg.body()).toBuffer())
        );
    }
}
