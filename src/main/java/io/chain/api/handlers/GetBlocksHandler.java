package io.chain.api.handlers;

import io.chain.models.Blockchain;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;

@RequiredArgsConstructor
public class GetBlocksHandler extends AbstractRouteHandler implements Handler<RoutingContext> {

    private final Blockchain blockchain;

    @Override
    public void handle(RoutingContext context) {
        addResponseHeaders(OK, context).end(blockchain.toJson().encodePrettily());
    }
}
