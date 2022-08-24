package io.chain.p2p.handlers;

import io.chain.models.Block;
import io.chain.models.Blockchain;
import io.chain.models.Transaction;
import io.chain.models.UTxOSet;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static io.chain.LocalMapConstants.UNCONFIRMED_TX_POOL;
import static io.chain.p2p.EventBusAddresses.SYNC_WALLET;

@RequiredArgsConstructor
public final class NewBlockHandler implements Handler<Message<String>> {

    private final Blockchain blockchain;
    private final UTxOSet utxoSet;
    private final Vertx vertx;

    @Override
    public void handle(Message<String> message) {
        try {
            final Block lastBlock = blockchain.getBlocks().get(blockchain.getBlocks().size() - 1);
            final List<Transaction> blockTxs = lastBlock.getTransactions();
            final List<Transaction> processedTxs = utxoSet.process(blockTxs);
            vertx.sharedData().getLocalMap(UNCONFIRMED_TX_POOL).clear();
            vertx.eventBus().send(
                SYNC_WALLET.getAddress(),
                "",
                new DeliveryOptions().setLocalOnly(true)
            );
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
