package io.chain.verticles;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.chain.MainVerticle;
import io.chain.models.UTxOSet;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import lombok.AccessLevel;
import lombok.Setter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.chain.p2p.handlers.NewUnconfirmedTransactionHandler.UNCONFIRMED_TX_POOL;

@ExtendWith(VertxExtension.class)
public abstract class AbstractVerticleTest {

    @Setter(AccessLevel.PRIVATE)
    protected WebClient client;

    protected UTxOSet configureInitialUTxOSet() {
        return new UTxOSet();
    }

    @BeforeEach
    private void setup(Vertx vertx, VertxTestContext context) throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonObject utxos = new JsonObject()
                .put("initialUTxOs", new JsonArray(mapper.writeValueAsString(configureInitialUTxOSet().getUtxos().values())));
        vertx.deployVerticle(
            MainVerticle.class,
            new DeploymentOptions().setConfig(utxos),
            result -> context.completeNow()
        );
        client = WebClient.create(vertx);
        vertx.sharedData().getLocalMap(UNCONFIRMED_TX_POOL).clear();
    }

    @AfterEach
    private void cleanUp(Vertx vertx, VertxTestContext context) {
        if (client != null) {
            client.close().onSuccess(v -> context.completeNow()).onFailure(context::failNow);
        }
    }

}
