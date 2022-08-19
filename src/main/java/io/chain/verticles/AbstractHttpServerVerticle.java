package io.chain.verticles;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static io.vertx.core.Future.succeededFuture;
import static java.lang.String.format;
import static java.util.Objects.isNull;

@Getter
@RequiredArgsConstructor
abstract class AbstractHttpServerVerticle extends AbstractConfiguredVerticle {
    protected final static Logger LOGGER = LoggerFactory.getLogger(AbstractHttpServerVerticle.class);

    private HttpServer server;

    @Override
    public void start(Promise<Void> startPromise) {
        retrieveConfig()
            .compose(this::createHttpServerOptions)
            .compose(this::createHttpServer)
            .onSuccess(httpServer -> {
                this.server = httpServer;
                LOGGER.info(format("%s listening on: http://localhost:%d", getClass().getSimpleName(), httpServer.actualPort()));
                startPromise.complete();
            })
            .onFailure(startPromise::fail);
    }

    @Override
    public void stop(Promise<Void> stopPromise) {
        if (isNull(server)) {
            stopPromise.complete();
            return;
        }

        server
            .close()
            .onSuccess(stopPromise::complete)
            .onFailure(stopPromise::fail);
    }

    protected abstract Future<HttpServer> configureHttpServer(HttpServer server);

    private Future<HttpServer> createHttpServer(HttpServerOptions opts) {
        return configureHttpServer(vertx.createHttpServer(opts))
                .compose(HttpServer::listen);
    }
}
