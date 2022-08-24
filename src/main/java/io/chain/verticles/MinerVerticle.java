package io.chain.verticles;

import io.chain.models.*;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonArray;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static io.chain.p2p.EventBusAddresses.NEW_BLOCK;
import static io.chain.p2p.EventBusAddresses.NEW_BLOCKCHAIN;
import static io.chain.p2p.handlers.NewUnconfirmedTransactionHandler.UNCONFIRMED_TX_POOL;

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
        final List<Transaction> unconfirmedTxs = new ArrayList<>(
            vertx.sharedData().<String, Transaction>getLocalMap(UNCONFIRMED_TX_POOL).values()
        );
        unconfirmedTxs.add(Transaction.rewardTransaction(wallet));
        vertx.
            <Block>executeBlocking(future ->
                future.complete(blockchain.addBlock(unconfirmedTxs, data))
            ).onSuccess(block -> {
                final JsonArray jsonBlockchain = blockchain.toJson();
                vertx
                    .eventBus()
                    .request(
                        NEW_BLOCK.getAddress(),
                        jsonBlockchain.getJsonObject(jsonBlockchain.size() - 1),
                        new DeliveryOptions().setLocalOnly(true),
                        reply -> {
                            if (reply.failed()) {
                                reply.cause().printStackTrace(System.err);
                                startPromise.fail(reply.cause());
                                return;
                            }
                            try {
                                utxoSet.sync(wallet);
                            } catch (Wallet.MismatchingUTxOAddressException e) {
                                e.printStackTrace(System.err);
                            }
                            vertx
                                .eventBus()
                                .publish(NEW_BLOCKCHAIN.getAddress(), blockchain.toJson());
                            startPromise.complete();
                        });
            });
    }
}
