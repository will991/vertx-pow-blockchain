package io.chain.api.handlers;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.chain.models.Block.genesisBlock;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("POST /mine")
public final class MineBlockHandlerTests extends AbstractApiTest {

    @Test
    @DisplayName("test mine/ post new block")
    void testMineBlock(Vertx vertx, VertxTestContext testContext) {
        final Buffer expected = Buffer.buffer("new block");
        post("/mine")
            .sendBuffer(expected, testContext.succeeding(response -> testContext.verify(() -> {
                final JsonArray blocks = response.bodyAsJsonArray();
                assertThat(blocks.getJsonObject(0).encode())
                    .isEqualTo(mapper.writeValueAsString(genesisBlock()));
                assertThat(blocks.getJsonObject(1).getBuffer("data"))
                    .isEqualTo(expected);
                assertThat(blocks.getJsonObject(1).getString("previousBlockHash"))
                    .isEqualTo(genesisBlock().getHash());
                testContext.completeNow();
            })));
    }
}
