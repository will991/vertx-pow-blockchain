package io.chain.verticles;

import io.chain.api.handlers.AbstractApiTest;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.chain.LocalMapConstants.UNCONFIRMED_TX_POOL;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Test signed unconfirmed tx pool")
public final class TransactionPoolVerticleTests extends AbstractApiTest {

    @Test
    @DisplayName("test valid transaction to be added to pool")
    void testValidTransactionPooling(Vertx vertx, VertxTestContext context) {
        assertThat(vertx.sharedData().getLocalMap(UNCONFIRMED_TX_POOL).isEmpty()).isEqualTo(true);

        post("/mine")
            .as(BodyCodec.buffer())
            .send(context.succeeding(__ -> context.verify(() -> {
                vertx.setTimer(3000L, timer-> {
                    get("/miner")
                        .as(BodyCodec.buffer())
                        .send(context.succeeding(response -> context.verify(() -> {
                            final JsonObject miner = response.bodyAsJsonObject();
                            final JsonObject payload = new JsonObject()
                                    .put("recipient", miner.getString("pk"))
                                    .put("amount", 25);
                            post("/transaction")
                                .as(BodyCodec.buffer())
                                .sendBuffer(payload.toBuffer(), context.succeeding(mempoolResponse -> context.verify(() -> {
                                    if (mempoolResponse.body().toJson() instanceof JsonObject) {
                                        context.failNow(mempoolResponse.bodyAsJsonObject().encodePrettily());
                                        return;
                                    }

                                    final JsonArray arr = mempoolResponse.bodyAsJsonArray();
                                    assertThat(arr.size()).isEqualTo(1);
                                    context.completeNow();
                                })));
                        })));
                });
            })));
    }
}
