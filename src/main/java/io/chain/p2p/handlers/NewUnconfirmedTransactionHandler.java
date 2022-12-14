package io.chain.p2p.handlers;

import io.chain.models.Transaction;
import io.chain.models.UTxOSet;
import io.chain.models.exceptions.TransactionValidationException;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class NewUnconfirmedTransactionHandler implements Handler<Message<JsonObject>> {
    public static final String UNCONFIRMED_TX_POOL = "io.chain.unconfirmed.txs.pool";

    private final String uuid;
    private final UTxOSet utxos;
    private final Vertx vertx;

    @Override
    public void handle(Message<JsonObject> msg) {
        final Transaction tx = Json.decodeValue(msg.body().encode(), Transaction.class);
        try {
            Transaction.validate(tx, utxos);
            vertx
                .sharedData()
                .<String, JsonObject>getLocalMap(UNCONFIRMED_TX_POOL)
                .put(tx.hash(), tx.toJson());
        } catch (TransactionValidationException e) {
            e.printStackTrace(System.err);
            msg.reply(e.toJson(), new DeliveryOptions().setLocalOnly(true));
        }
    }
}
