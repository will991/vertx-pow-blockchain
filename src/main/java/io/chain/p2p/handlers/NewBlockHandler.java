package io.chain.p2p.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.chain.models.Block;
import io.chain.models.Transaction;
import io.chain.models.UTxOSet;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static io.chain.p2p.handlers.NewUnconfirmedTransactionHandler.UNCONFIRMED_TX_POOL;
import static java.lang.String.format;

@RequiredArgsConstructor
public final class NewBlockHandler implements Handler<Message<JsonArray>> {

    private final UTxOSet utxoSet;
    private final Vertx vertx;

    @Override
    public void handle(Message<JsonArray> message) {
        final JsonObject blockJson = message.body().getJsonObject(message.body().size() - 1);
        final Block lastBlock;
        try {
            lastBlock = new ObjectMapper().readValue(blockJson.encode(), Block.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace(System.err);
            message.reply("Could not deserialize block data.");
            return;
        }

        try {
            final JsonObject blockData = Buffer.buffer(lastBlock.getData()).toJsonObject();
            final List<Transaction> blockTxs = new ArrayList<>();

            for (final Object raw: blockData.getJsonArray("txs")) {
                final JsonObject rawTx = (JsonObject) raw;
                blockTxs.add(Json.decodeValue(rawTx.toBuffer(), Transaction.class));
            }

            final List<Transaction> processedTxs = utxoSet.process(blockTxs);
            processedTxs.forEach(tx ->
                vertx.sharedData().getLocalMap(UNCONFIRMED_TX_POOL).remove(tx.hash())
            );
        } catch (Exception e) {
            message.reply(format("Could not deserialize block data for: %s", lastBlock.getHash()));
        }
    }
}
