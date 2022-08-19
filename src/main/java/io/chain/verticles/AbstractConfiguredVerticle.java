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
import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.lang.System.lineSeparator;
import static java.util.Objects.isNull;

abstract class AbstractConfiguredVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractConfiguredVerticle.class);

    protected abstract String getPortConfigKey();
    protected abstract int getDefaultPort();

    protected static final ConfigStoreOptions DEFAULT_OPTIONS =
        new ConfigStoreOptions()
            .setType("file")
            .setFormat("json")
            .setConfig(new JsonObject().put("path", "config.json"));

    protected final Future<JsonObject> retrieveConfig() {
        final Promise<JsonObject> promise = Promise.promise();
        final ConfigRetrieverOptions opts = new ConfigRetrieverOptions()
                                                    .addStore(DEFAULT_OPTIONS);
        ConfigRetriever
            .create(vertx, opts)
            .getConfig()
            .onSuccess(config -> {
                LOGGER.info(format("Retrieved configuration:%s%s", lineSeparator(), config.encodePrettily()));
                promise.complete(config);
            })
            .onFailure(promise::fail);

        return promise.future();
    }

    protected final Future<HttpServerOptions> createHttpServerOptions(JsonObject config) {
        final JsonObject defaultConfig = new JsonObject()
                                            .put("host", "localhost")
                                            .put("port", getPort());
        final JsonObject http = config.getJsonObject(getPortConfigKey(), defaultConfig);
        final boolean hasCustomPort = ! isNull(System.getProperty(getPortConfigKey()));
        return succeededFuture(
                new HttpServerOptions()
                        .setHost(http.getString("host", defaultConfig.getString("host")))
                        .setPort(hasCustomPort ? getPort() : http.getInteger("port", defaultConfig.getInteger("port")))
                        .setCompressionSupported(true)
                        .setLogActivity(true)
        );
    }

    protected final int getPort() {
        try {
            return parseInt(System.getProperty(getPortConfigKey(), format("%d", getDefaultPort())));
        } catch (NumberFormatException nfe) {
            return getDefaultPort();
        }
    }
}
