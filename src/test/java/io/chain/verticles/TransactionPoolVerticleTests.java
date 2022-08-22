package io.chain.verticles;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.chain.models.*;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxTestContext;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.chain.p2p.EventBusAddresses.NEW_TRANSACTION;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Test signed unconfirmed tx pool")
public final class TransactionPoolVerticleTests extends AbstractVerticleTest {
    private static final String UNCONFIRMED_TX_POOL = "io.chain.unconfirmed.txs.pool";

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
        uTxOSet.add(wallet.getUtxos().get(0));
        return uTxOSet;
    }

    @Test
    @DisplayName("test valid transaction to be added to pool")
    void testValidTransactionPooling(Vertx vertx, VertxTestContext context) throws JsonProcessingException {
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
            context.completeNow();
        });
    }
}
