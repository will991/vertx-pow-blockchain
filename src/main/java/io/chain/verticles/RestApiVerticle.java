package io.chain.verticles;

import io.chain.api.RestApiRouter;
import io.chain.models.Blockchain;
import io.chain.models.UTxOSet;
import io.chain.models.Wallet;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static io.vertx.core.Future.succeededFuture;

@Getter
@RequiredArgsConstructor
public final class RestApiVerticle extends AbstractHttpServerVerticle {

    private final Blockchain blockchain;
    private final Wallet wallet;
    private final UTxOSet utxos;

    @Override
    protected Future<HttpServer> configureHttpServer(HttpServer server) {
        return succeededFuture(
            server.requestHandler(new RestApiRouter(vertx, blockchain, wallet, utxos))
        );
    }
}
