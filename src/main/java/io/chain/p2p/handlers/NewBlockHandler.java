package io.chain.p2p.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.chain.models.Block;
import io.chain.models.Transaction;
import io.chain.models.UTxOSet;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static io.chain.p2p.handlers.NewUnconfirmedTransactionHandler.UNCONFIRMED_TX_POOL;

@RequiredArgsConstructor
public final class NewBlockHandler implements Handler<Message<JsonObject>> {

    private final UTxOSet utxoSet;
    private final Vertx vertx;

    @Override
    public void handle(Message<JsonObject> message) {
        try {
            final JsonObject blockJson = message.body();
            final Block lastBlock;
            try {
                lastBlock = new ObjectMapper().readValue(blockJson.encode(), Block.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace(System.err);
                message.fail(400, "Could not deserialize block data.");
                return;
            }

            final List<Transaction> blockTxs = lastBlock.getTransactions();
            final List<Transaction> processedTxs = utxoSet.process(blockTxs);

            processedTxs
                .forEach(tx ->
                    vertx.sharedData().getLocalMap(UNCONFIRMED_TX_POOL).remove(tx.hash())
                );
            message.reply("");
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
