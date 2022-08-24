package io.chain.api.handlers;

import io.chain.models.UTxOSet;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class GetUTxOSetHandler extends AbstractRouteHandler implements Handler<RoutingContext> {

    private final UTxOSet utxoSet;

    @Override
    public void handle(RoutingContext routingContext) {
        final JsonArray availableUTxOs = utxoSet
                .getUtxos()
                .values()
                .stream()
                .reduce(
                    new JsonArray(),
                    (arr, utxo) -> arr.add(new JsonObject(Json.encode(utxo))),
                    JsonArray::addAll
                );

        addResponseHeaders(HttpResponseStatus.OK, routingContext)
            .end(availableUTxOs.encodePrettily());
    }
}
