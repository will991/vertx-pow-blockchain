package io.chain.api.handlers;

import io.chain.models.Blockchain;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetBlockCountHandler extends AbstractRouteHandler implements Handler<RoutingContext> {

    private final Blockchain blockchain;

    @Override
    public void handle(RoutingContext context) {
        addResponseHeaders(HttpResponseStatus.OK, context)
            .end(new JsonObject().put("height", blockchain.getBlocks().size()).encodePrettily());
    }
}
