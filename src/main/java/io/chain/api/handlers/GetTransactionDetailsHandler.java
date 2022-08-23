package io.chain.api.handlers;

import io.chain.models.Block;
import io.chain.models.Blockchain;
import io.chain.models.Transaction;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class GetTransactionDetailsHandler extends AbstractRouteHandler implements Handler<RoutingContext> {

    private final Blockchain blockchain;

    @Override
    public void handle(RoutingContext routingContext) {
        final String txHash = routingContext.request().getParam("txHash");
        System.out.println("Looking up: " + txHash);
        for (Block block: blockchain.getBlocks()) {
            for (Transaction tx: block.getTransactions()) {
                if (tx.hash().equals(txHash)) {
                    addResponseHeaders(HttpResponseStatus.OK, routingContext)
                        .end(tx.toJson().encodePrettily());
                    return;
                }
            }
        }

        addResponseHeaders(HttpResponseStatus.BAD_REQUEST, routingContext)
            .end(error("No matching transaction found."));
    }
}
