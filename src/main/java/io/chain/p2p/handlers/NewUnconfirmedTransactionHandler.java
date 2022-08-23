package io.chain.p2p.handlers;

import io.chain.models.Transaction;
import io.chain.models.UTxOSet;
import io.chain.models.exceptions.TransactionValidationException;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import lombok.RequiredArgsConstructor;

import static java.lang.String.format;

@RequiredArgsConstructor
public final class UnconfirmedTransactionHandler implements Handler<Message<String>> {
    public static final String UNCONFIRMED_TX_POOL = "io.chain.unconfirmed.txs.pool";

    private final String uuid;
    private final UTxOSet utxos;
    private final Vertx vertx;

    @Override
    public void handle(Message<String> msg) {
        final Transaction tx = Json.decodeValue(msg.body(), Transaction.class);

        System.out.println(format("[%s] MemPool received Tx: %s", uuid, tx.hash()));
        try {
            Transaction.validate(tx, utxos);
            vertx
                .sharedData()
                .<String, Transaction>getLocalMap(UNCONFIRMED_TX_POOL)
                .put(tx.hash(), tx);
        } catch (TransactionValidationException e) {
            msg.reply(e.toJson(), new DeliveryOptions().setLocalOnly(true));
        }
    }
}
