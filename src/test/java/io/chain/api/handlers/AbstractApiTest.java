package io.chain.api.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.chain.MainVerticle;
import io.chain.verticles.AbstractVerticleTest;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

public abstract class AbstractApiTest extends AbstractVerticleTest {
    private final static int DEFAULT_TEST_PORT = 9001;

    protected final ObjectMapper mapper = new ObjectMapper();

    protected final Future<WebClient> launchClient(Vertx vertx, int port) {
        final Promise<WebClient> promise = Promise.promise();

        final JsonObject httpConfig = new JsonObject()
                .put("host", "localhost")
                .put("port", port);
        final JsonObject config = new JsonObject().put("http", httpConfig);

        vertx
            .deployVerticle(MainVerticle.class, new DeploymentOptions().setConfig(config))
            .onSuccess(r -> promise.complete(
                WebClient.create(
                    vertx,
                    new WebClientOptions().setDefaultHost("localhost").setDefaultPort(port)
                )
            ))
            .onFailure(promise::fail);
        return promise.future();
    }

    protected final HttpRequest<Buffer> get(String requestURI) {
        return get(DEFAULT_TEST_PORT, requestURI, client);
    }

    protected final HttpRequest<Buffer> get(int port, String requestURI, WebClient client) {
        return client.get(port, "localhost", requestURI);
    }

    protected final HttpRequest<Buffer> post(String requestURI) {
        return post(DEFAULT_TEST_PORT, requestURI, client);
    }

    protected final HttpRequest<Buffer> post(int port, String requestURI, WebClient client) {
        return client.post(port, "localhost", requestURI);
    }
}
