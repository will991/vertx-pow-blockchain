package io.chain.models;

import com.starkbank.ellipticcurve.PrivateKey;
import com.starkbank.ellipticcurve.PublicKey;
import io.chain.models.exceptions.InsufficientUTxOBalanceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@DisplayName("Wallet Tests")
public final class WalletTests {

    private Wallet wallet;
    private UTxOSet utxoSet = new UTxOSet();

    @BeforeEach
    void setup() throws Wallet.MismatchingUTxOAddressException {
        wallet = new Wallet();
        final UTxO utxo = new UTxO(new Input("123".getBytes(), 0), new Output(wallet.getPk(), 50));
        wallet.addUTxO(utxo);

        final UTxO utxo2 = new UTxO(new Input("456".getBytes(), 0), new Output(wallet.getPk(), 100));
        utxoSet.add(utxo2);
    }

    @Test
    @DisplayName("test wallet balance and addition of utxo")
    void testWalletBalance() {
        assertThat(wallet.balance()).isEqualTo(50);
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

    @Test
    @DisplayName("test utxoSet wallet sync")
    void testUTxOSetWalletSync() {
        try {
            assertThat(wallet.getUTxOs().size()).isEqualTo(1);
            assertThat(wallet.balance()).isEqualTo(50);
            utxoSet.sync(wallet);
            assertThat(wallet.getUTxOs().size()).isEqualTo(1);
            assertThat(wallet.balance()).isEqualTo(100);
        } catch (Wallet.MismatchingUTxOAddressException e) {
            fail("Expected no MismatchingUTxOAddressException exception");
        }
    }

    @Test
    @DisplayName("test transaction construction with insufficient UTxO set")
    void testWalletTxConstructionWithInsuffiecientUTxOSet() {
        final PublicKey pk = new PrivateKey().publicKey();
        try {
            wallet.create(pk, 55);
            fail("Expected InsufficientUTxOBalanceException");
        } catch (InsufficientUTxOBalanceException e) {
            /* expected */
        }
    }

    @Test
    @DisplayName("test transaction construction with sufficient UTxO set")
    void testWalletTxConstructionWithSuffiecientUTxOSet() {
        final PublicKey recipient = new PrivateKey().publicKey();
        try {
            Transaction transaction = wallet.create(recipient, 35);
            assertThat(transaction.getInputs().size()).isEqualTo(1);
            assertThat(transaction.getOutputs().size()).isEqualTo(2);
            assertThat(transaction.getOutputs().get(0).getAmount()).isEqualTo(35);
            assertThat(transaction.getOutputs().get(0).getAddress()).isEqualTo(recipient);
            assertThat(transaction.getOutputs().get(1).getAmount()).isEqualTo(15);
            assertThat(transaction.getOutputs().get(1).getAddress()).isEqualTo(wallet.getPk());
            assertThat(transaction.getData()).isNull();
        } catch (InsufficientUTxOBalanceException e) {
            fail("Expected InsufficientUTxOBalanceException");
        }
    }

    @Test
    @DisplayName("test transaction construction with sufficient UTxO set and no change")
    void testWalletTxConstructionWithSuffiecientUTxOSetAndNoChange() {
        final PublicKey recipient = new PrivateKey().publicKey();
        try {
            Transaction transaction = wallet.create(recipient, 50);
            assertThat(transaction.getInputs().size()).isEqualTo(1);
            assertThat(transaction.getOutputs().size()).isEqualTo(1);
            assertThat(transaction.getOutputs().get(0).getAmount()).isEqualTo(50);
            assertThat(transaction.getOutputs().get(0).getAddress()).isEqualTo(recipient);
            assertThat(transaction.getData()).isNull();
        } catch (InsufficientUTxOBalanceException e) {
            fail("Expected no exception");
        }
    }
}
