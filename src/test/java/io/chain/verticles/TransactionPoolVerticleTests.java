package io.chain.verticles;

import io.chain.api.handlers.AbstractApiTest;
import io.chain.models.*;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.chain.p2p.EventBusAddresses.NEW_TRANSACTION;
import static io.chain.p2p.handlers.UnconfirmedTransactionHandler.UNCONFIRMED_TX_POOL;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Test signed unconfirmed tx pool")
public final class TransactionPoolVerticleTests extends AbstractApiTest {

    private Wallet wallet;
    private Transaction signedTx;

    @Override
    protected UTxOSet configureInitialUTxOSet() {
        wallet = new Wallet();
        final UTxO utxo = new UTxO(new Input("123".getBytes(), 0), new Output(wallet.getPk(), 100));
        signedTx = wallet.sign(new Transaction(
            singletonList(utxo.getTxIn()),
            singletonList(new Output(wallet.getPk(), 25))
        ));

        try {
            wallet.addUTxO(utxo);
        } catch (Wallet.MismatchingUTxOAddressException e) {
            e.printStackTrace();
        }
        UTxOSet uTxOSet = new UTxOSet();
        uTxOSet.add(wallet.getUTxOs().get(0));
        return uTxOSet;
    }

    @Test
    @DisplayName("test valid transaction to be added to pool")
    void testValidTransactionPooling(Vertx vertx, VertxTestContext context) {
        assertThat(vertx.sharedData().getLocalMap(UNCONFIRMED_TX_POOL).isEmpty()).isEqualTo(true);

        vertx.eventBus().publish(
            NEW_TRANSACTION.getAddress(),
            signedTx.toJson().encode()
        );

        vertx.setTimer(1000L, __ -> {
            final Map<String, Transaction> map = vertx.sharedData().getLocalMap(UNCONFIRMED_TX_POOL);
            assertThat(map.size()).isEqualTo(1);
            final Transaction t = map.get(signedTx.hash());
            assertThat(t.hash()).isEqualTo(signedTx.hash());

            get("/mempool")
                .as(BodyCodec.buffer())
                .send(context.succeeding(response -> context.verify(() -> {
                    assertThat(response.bodyAsJsonArray().encodePrettily())
                        .isEqualTo(new JsonArray().add(new JsonObject(Json.encode(signedTx))).encodePrettily());
                    context.completeNow();
                })));
        });
    }
}
