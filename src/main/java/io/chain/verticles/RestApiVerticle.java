package io.chain.verticles;

import io.chain.api.RestApiRouter;
import io.chain.models.Blockchain;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class RestApiVerticle extends AbstractHttpServerVerticle {

    private final Blockchain blockchain;

    @Override
    protected Future<HttpServer> configureHttpServer(HttpServer server) {
        return Future.succeededFuture(
            server.requestHandler(new RestApiRouter(vertx, blockchain))
        );
    }

    @Override
    protected String getPortConfigKey() {
        return "rest.http.port";
    }

    @Override
    protected int getDefaultPort() { return 8080; }
}
