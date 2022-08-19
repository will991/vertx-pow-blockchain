package io.chain.api.handlers;

import io.chain.models.Block;
import io.vertx.core.Vertx;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GET /blocks")
public final class GetBlocksHandlerTests extends AbstractApiTest {

    @Test
    @DisplayName("test get genesis blockchain")
    void testGetGenesisBlockchain(Vertx vertx, VertxTestContext testContext) {
        get("/blocks")
            .as(BodyCodec.buffer())
            .send(testContext.succeeding(response -> testContext.verify(() -> {
                assertThat(
                    response.bodyAsJsonArray().getJsonObject(0).encode()
                ).isEqualTo(
                    mapper.writeValueAsString(Block.genesisBlock())
                );
                testContext.completeNow();
            })));
    }

}
