package io.chain.api.handlers;

import com.fasterxml.jackson.core.type.TypeReference;
import io.chain.models.Block;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.chain.models.Block.genesisBlock;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("POST /mine")
public final class MineBlockHandlerTests extends AbstractApiTest {

    @Test
    @DisplayName("test mine/ post new block")
    void testMineBlock(Vertx vertx, VertxTestContext testContext) {
        final String expectedData = "new block";
        post("/mine")
            .sendBuffer(Buffer.buffer(expectedData.getBytes()), testContext.succeeding(response -> testContext.verify(() -> {
                final List<Block> blocks = mapper.readValue(response.bodyAsJsonArray().encode(), new TypeReference<>(){});
                assertThat(blocks.size()).isEqualTo(2);
                assertThat(blocks.get(0).toJson().encodePrettily())
                    .isEqualTo(genesisBlock().toJson().encodePrettily());
                assertThat(blocks.get(1).getUTF_8Data())
                    .isEqualTo(expectedData);
                assertThat(blocks.get(1).getPreviousBlockHash())
                    .isEqualTo(genesisBlock().getHash());

                get("/miner")
                    .as(BodyCodec.buffer())
                    .send(testContext.succeeding(getResponse -> testContext.verify(() -> {
                        final JsonObject minerInfo = getResponse.bodyAsJsonObject();
                        assertThat(minerInfo.getInteger("balance")).isEqualTo(100);
                        testContext.completeNow();
                    })));
            })));
    }
}
