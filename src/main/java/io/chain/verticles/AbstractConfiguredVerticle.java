package io.chain.verticles;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;

import static io.vertx.core.Future.succeededFuture;
import static java.lang.String.format;
import static java.lang.System.lineSeparator;

abstract class AbstractConfiguredVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractConfiguredVerticle.class);

    protected static final ConfigStoreOptions DEFAULT_OPTIONS =
        new ConfigStoreOptions()
                .setType("file")
                .setFormat("json")
                .setOptional(false)
                .setConfig(new JsonObject().put("path", "config.json"));

    protected final Future<JsonObject> retrieveConfig() {
        final Promise<JsonObject> promise = Promise.promise();
        final ConfigRetrieverOptions opts = new ConfigRetrieverOptions()
                                                    .addStore(DEFAULT_OPTIONS);
        ConfigRetriever
            .create(vertx, opts)
            .getConfig()
            .compose(config -> succeededFuture(config.mergeIn(config())))
            .onSuccess(config -> {
                if (System.getProperty("http.port") != null) {
                    config.getJsonObject("http").put("port", Integer.valueOf(System.getProperty("http.port")));
                }
                LOGGER.info(format("Retrieved configuration:%s%s", lineSeparator(), config.encodePrettily()));
                promise.complete(config);
            })
            .onFailure(promise::fail);
        return promise.future();
    }

    protected final Future<HttpServerOptions> createHttpServerOptions(JsonObject config) {
        final JsonObject httpConfig = config.getJsonObject("http");
        return succeededFuture(
            new HttpServerOptions()
                    .setHost(httpConfig.getString("host"))
                    .setPort(httpConfig.getInteger("port"))
                    .setCompressionSupported(true)
                    .setLogActivity(true)
        );
    }
}
