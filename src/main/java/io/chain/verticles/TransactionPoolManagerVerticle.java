package io.chain.verticles;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.chain.models.Transaction;
import io.chain.models.UTxO;
import io.chain.models.UTxOSet;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static io.chain.p2p.EventBusAddresses.NEW_TRANSACTION;
import static java.lang.String.format;

@RequiredArgsConstructor
public final class TransactionPoolManagerVerticle extends AbstractEventBusVerticle {

    public static final String UNCONFIRMED_TX_POOL = "io.chain.unconfirmed.txs.pool";
    private final String uuid;
    private final UTxOSet utxos;

    @Override
    public void start(Promise<Void> startPromise) {
        this.<String>register(NEW_TRANSACTION, msg -> {
            final Transaction tx = Json.decodeValue(msg.body(), Transaction.class);
            if (Transaction.isValid(tx, utxos)) {
                LOGGER.info(format("[%s] Added transaction to pool", uuid));
                vertx
                    .sharedData()
                    .<String, Transaction>getLocalMap(UNCONFIRMED_TX_POOL)
                    .put(tx.hash(), tx);
            }
        });

        initUTxOSet()
            .onSuccess(startPromise::complete)
            .onFailure(startPromise::fail);
    }

    private Future<Void> initUTxOSet() {
        final JsonArray initialUTxOs = config().getJsonArray("initialUTxOs");
        try {
            final ObjectMapper mapper = new ObjectMapper();
            List<UTxO> us = mapper.readValue(initialUTxOs.encode(), new TypeReference<>() {});
            us.forEach(utxos::add);
            return Future.succeededFuture();
        } catch (JsonProcessingException e) {
            e.printStackTrace(System.err);
            return Future.failedFuture(e);
        }
    }
}
