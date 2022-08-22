package io.chain.api.handlers;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.junit5.VertxTestContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Blockchain Sync Tests")
public final class BlockchainSyncTests extends AbstractApiTest {

//    @Test
//    @DisplayName("test sync of blockchain across two instances")
//    void testSyncOfBlockchainsAcrossTwoInstances(Vertx vertx, VertxTestContext testContext) {
//        int client2Port = 9001;
//        launchClient(vertx, client2Port)
//            .onFailure(testContext::failNow)
//            .onSuccess(client2 -> {
//                // Mine block #2 on newly launched node #2 & check sync on node #1
//                post(client2Port, "/mine", client2)
//                    .sendBuffer(Buffer.buffer("block #2"), testContext.succeeding(response -> testContext.verify(() ->
//                        get(8080, "/blockCount", client)
//                        .as(BodyCodec.buffer())
//                        .send(testContext.succeeding(response2 -> testContext.verify(() -> {
//                            Assertions.assertThat(response2.bodyAsJsonObject().getInteger("height")).isEqualTo(1);
//                            testContext.completeNow();
//                        })))
//                    )));
//            });
//    }
}
