package io.chain.verticles;

import io.chain.models.*;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

import static io.chain.LocalMapConstants.UNCONFIRMED_TX_POOL;
import static io.chain.p2p.EventBusAddresses.NEW_BLOCKCHAIN;

@RequiredArgsConstructor
public final class MinerVerticle extends AbstractVerticle {

    private final Blockchain blockchain;
    private final UTxOSet utxoSet;
    private final Wallet wallet;
    private final byte[] data;

    @Override
    public void start(Promise<Void> startPromise) {
        /*
         * (1) Grabs txs from pool
         * (1.1) Add reward tx for mining block
         * (2) Take txs and create block whose data consists of those txs
         * (3) Tell p2p to sync chains and include new block with those transactions
         * (4) Every other node now needs to remove txs from their mem pool since they are now confirmed
         */
        final List<Transaction> unconfirmedTxs =
            vertx
                .sharedData()
                .<String, JsonObject>getLocalMap(UNCONFIRMED_TX_POOL)
                .values()
                .stream()
                .map(t -> Json.decodeValue(t.toBuffer(), Transaction.class)).collect(Collectors.toList());
        unconfirmedTxs.add(Transaction.rewardTransaction(wallet));

        vertx.
            <Block>executeBlocking(future ->
                future.complete(blockchain.addBlock(unconfirmedTxs, data))
            ).onSuccess(block -> {
                /* NOTE: Triggers blockchain update */
                vertx
                    .eventBus()
                    .publish(NEW_BLOCKCHAIN.getAddress(), blockchain.toJson());
                startPromise.complete();
            });
    }
}
