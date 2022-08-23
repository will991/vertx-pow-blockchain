package io.chain.api.handlers;

import io.chain.models.Block;
import io.chain.models.Blockchain;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class GetBlockDetailsHandler extends AbstractRouteHandler implements Handler<RoutingContext> {

    private final Blockchain blockchain;

    @Override
    public void handle(RoutingContext routingContext) {
        final String blockHash = routingContext.request().getParam("blockHash");
        System.out.println("Looking up block hash : " + blockHash);
        for (Block block: blockchain.getBlocks()) {
            if (block.getHash().equals(blockHash)) {
                addResponseHeaders(HttpResponseStatus.OK, routingContext)
                    .end(block.toJson().encodePrettily());
                return;
            }
        }

        addResponseHeaders(HttpResponseStatus.BAD_REQUEST, routingContext)
                .end(error("No matching block found."));
    }
}
