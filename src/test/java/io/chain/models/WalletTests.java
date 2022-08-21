package io.chain.models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@DisplayName("Wallet Tests")
public final class WalletTests {

    @Test
    @DisplayName("test wallet balance and addition of utxo")
    void testWalletBalance() {
        try {
            final Wallet wallet = new Wallet();
            final UTxO utxo = new UTxO(new Input("123".getBytes(), 0), new Output(wallet.getPk(), 50));
            wallet.addUTxO(utxo);
            assertThat(wallet.balance()).isEqualTo(50);
        } catch (Wallet.MismatchingUTxOAddressException e) {
            fail("No exception expected", e);
        }
    }

    @Test
    @DisplayName("test invalid utxo wallet set")
    void testInvalidUTxOWallet() {
        final Wallet w1 = new Wallet();
        final Wallet w2 = new Wallet();
        try {
            final UTxO utxo = new UTxO(new Input("123".getBytes(), 0), new Output(w1.getPk(), 50));
            w2.addUTxO(utxo);
            fail("Expected MismatchingUTxOAddressException exception");
        } catch (Wallet.MismatchingUTxOAddressException e) {
            /* expected */
        }
    }
}
