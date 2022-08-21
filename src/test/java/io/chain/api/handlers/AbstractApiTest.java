package io.chain.api.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.chain.MainVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
abstract class AbstractApiTest {
    protected final static Logger LOGGER = LoggerFactory.getLogger(AbstractApiTest.class);
    private final static int DEFAULT_TEST_PORT = 9001;

    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.PRIVATE)
    protected static WebClient client;

    protected final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    final void setup(Vertx vertx, VertxTestContext context) {
        vertx.deployVerticle(MainVerticle.class, new DeploymentOptions(), result -> context.completeNow());
        client = WebClient.create(vertx);
    }

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
