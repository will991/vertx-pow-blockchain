package io.chain.api.handlers;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GET /blockCount")
public final class GetBlockCountHandlerTests extends AbstractApiTest {

    @Test
    @DisplayName("test get block count for genesis blockchain")
    void testGetGenesisBlockCount(Vertx vertx, VertxTestContext testContext) {
        get("/blockCount")
            .as(BodyCodec.buffer())
            .send(testContext.succeeding(response -> testContext.verify(() -> {
                assertThat(response.bodyAsJsonObject().getInteger("height")).isEqualTo(1);
                testContext.completeNow();
            })));
    }

    @Test
    @DisplayName("test get block count for larger blockchain")
    void testGetBlockCount(Vertx vertx, VertxTestContext testContext) {
        post("/mine")
            .sendBuffer(Buffer.buffer("test"), aR -> {
                if (aR.failed())  {
                    testContext.failNow(aR.cause());
                    return;
                }

                get("/blockCount")
                    .as(BodyCodec.buffer())
                    .send(testContext.succeeding(response -> testContext.verify(() -> {
                        assertThat(response.bodyAsJsonObject().getInteger("height")).isEqualTo(2);
                        testContext.completeNow();
                    })));
            });
    }
}
