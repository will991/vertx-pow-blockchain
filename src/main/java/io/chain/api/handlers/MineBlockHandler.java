package io.chain.api.handlers;

import io.chain.models.Blockchain;
import io.chain.models.UTxOSet;
import io.chain.models.Wallet;
import io.chain.verticles.MinerVerticle;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class MineBlockHandler extends AbstractRouteHandler implements Handler<RoutingContext> {
    private final Blockchain blockchain;
    private final UTxOSet utxos;
    private final Wallet minerWallet;
    private final Vertx vertx;

    @Override
    public void handle(RoutingContext context) {
        final byte[] blockData = context.body() == null ? null : context.body().buffer().getBytes();

        vertx
            .deployVerticle(new MinerVerticle(blockchain, utxos, minerWallet, blockData), new DeploymentOptions())
            .onSuccess(s -> context.next())
            .onFailure(err ->
                addResponseHeaders(HttpResponseStatus.BAD_REQUEST, context)
                    .end(error(err.getMessage()))
            );
    }
}
