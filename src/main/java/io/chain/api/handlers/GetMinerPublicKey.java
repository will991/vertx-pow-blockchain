package io.chain.api.handlers;

import io.chain.models.Wallet;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.util.encoders.Hex;

@RequiredArgsConstructor
public final class GetMinerPublicKey extends AbstractRouteHandler implements Handler<RoutingContext> {

    private final Wallet wallet;

    @Override
    public void handle(RoutingContext routingContext) {
        addResponseHeaders(HttpResponseStatus.OK, routingContext)
            .end(
                new JsonObject()
                    .put("pk", Hex.toHexString(wallet.getPk().toByteString().getBytes()))
                    .put("balance", wallet.balance())
                    .encodePrettily()
            );
    }
}
