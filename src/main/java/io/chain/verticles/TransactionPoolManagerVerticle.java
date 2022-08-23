package io.chain.verticles;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.chain.models.UTxO;
import io.chain.models.UTxOSet;
import io.chain.p2p.handlers.NewBlockHandler;
import io.chain.p2p.handlers.NewUnconfirmedTransactionHandler;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static io.chain.p2p.EventBusAddresses.NEW_BLOCK;
import static io.chain.p2p.EventBusAddresses.NEW_TRANSACTION;

@RequiredArgsConstructor
public final class TransactionPoolManagerVerticle extends AbstractEventBusVerticle {

    private final UTxOSet utxos;
    private final String uuid;

    @Override
    public void start(Promise<Void> startPromise) {
        register(NEW_TRANSACTION, new NewUnconfirmedTransactionHandler(uuid, utxos, vertx));
        register(NEW_BLOCK, new NewBlockHandler(utxos, vertx));

        initUTxOSet()
            .onSuccess(startPromise::complete)
            .onFailure(startPromise::fail);
    }

    private Future<Void> initUTxOSet() {
        final JsonArray initialUTxOs = config().getJsonArray("initialUTxOs");
        if (initialUTxOs != null) {
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
        return Future.succeededFuture();
    }
}
