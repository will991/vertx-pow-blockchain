package io.chain.api.handlers;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;

import static io.chain.LocalMapConstants.UNCONFIRMED_TX_POOL;

@RequiredArgsConstructor
public final class GetMemPoolTransactionsHandler extends AbstractRouteHandler implements Handler<RoutingContext> {

    private final Vertx vertx;

    @Override
    public void handle(RoutingContext routingContext) {
        final JsonArray result = vertx
                .sharedData()
                .<String, JsonObject>getLocalMap(UNCONFIRMED_TX_POOL)
                .values()
                .stream()
                .reduce(new JsonArray(),
                        (arr, tx) -> {
                            arr.add(tx);
                            return arr;
                        },
                        (a1, a2) -> {
                            a1.addAll(a2);
                            return a1;
                        });

        addResponseHeaders(HttpResponseStatus.OK, routingContext).end(result.encodePrettily());
    }
}
