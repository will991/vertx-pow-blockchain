package io.chain.api.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.chain.MainVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

@ExtendWith(VertxExtension.class)
abstract class AbstractApiTest {
    protected final static Logger LOGGER = LoggerFactory.getLogger(AbstractApiTest.class);

    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.PRIVATE)
    protected static WebClient client;

    protected final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    final void setup(Vertx vertx, VertxTestContext context) {
        vertx.deployVerticle(MainVerticle.class, new DeploymentOptions(), result -> context.completeNow());
        client = WebClient.create(vertx);
    }

    protected final HttpRequest<Buffer> get(String requestURI) {
        return client.get(8080, "localhost", requestURI);
    }

    protected final HttpRequest<Buffer> post(String requestURI) {
        return client.post(8080, "localhost", requestURI);
    }
}
