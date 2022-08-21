package io.chain.models;

import com.starkbank.ellipticcurve.PrivateKey;
import com.starkbank.ellipticcurve.PublicKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public final class TransactionTests {

    private final PrivateKey sk = new PrivateKey();
    private final PublicKey pk = sk.publicKey();
    private Wallet wallet;
    private UTxOSet utxoSet;

    @BeforeEach
    void setupWallet() throws Wallet.MismatchingUTxOAddressException {
        final UTxO utxo = new UTxO(new Input("123".getBytes(), 0), new Output(pk, 50));
        utxoSet = new UTxOSet();
        wallet = new Wallet(sk, pk, utxo);
    }

    @Test
    @DisplayName("test valid transaction")
    void testValidTransaction() {
        final Wallet recipient = new Wallet();

        Transaction unsignedTx = new Transaction(
                singletonList(wallet.getUtxos().get(0).getTxIn()),
                Arrays.asList(
                    new Output(pk, 30),
                    new Output(recipient.getPk(), 20)
                ),
                null);

        Transaction signedTx = wallet.sign(unsignedTx);
        utxoSet.add(wallet.getUtxos().get(0));
        assertThat(Transaction.isValid(signedTx, utxoSet)).isEqualTo(true);

        utxoSet.process(Arrays.asList(signedTx));
        assertThat(utxoSet.contains(wallet.getUtxos().get(0))).isEqualTo(false);
        final Set<UTxO> w1UTxos = utxoSet.filterBy(pk);
        assertThat(w1UTxos.size()).isEqualTo(1);
        assertThat(w1UTxos.stream().findFirst().get().getTxOut().getAmount()).isEqualTo(30);

        final Set<UTxO> recipientUTxos = utxoSet.filterBy(recipient.getPk());
        assertThat(recipientUTxos.size()).isEqualTo(1);
        assertThat(recipientUTxos.stream().findFirst().get().getTxOut().getAmount()).isEqualTo(20);
    }

    @Test
    @DisplayName("test spending transaction with UTxO that's is not available")
    void testInvalidTransaction_001() {
        final Wallet recipient = new Wallet();

        Transaction unsignedTx = new Transaction(
                singletonList(wallet.getUtxos().get(0).getTxIn()),
                Arrays.asList(
                    new Output(pk, 30),
                    new Output(recipient.getPk(), 20)
                ),
                null);

        Transaction signedTx = wallet.sign(unsignedTx);
        assertThat(Transaction.isValid(signedTx, utxoSet)).isEqualTo(false);
    }


    @Test
    @DisplayName("test double spending within same tx")
    void testDoubleSpendingWithinSameTransaction() {
        final Wallet recipient = new Wallet();

        Transaction unsignedTx = new Transaction(
                Arrays.asList(
                    wallet.getUtxos().get(0).getTxIn(),
                    wallet.getUtxos().get(0).getTxIn()
                ),
                Arrays.asList(
                        new Output(pk, 30),
                        new Output(recipient.getPk(), 20)
                ),
                null);

        Transaction signedTx = wallet.sign(unsignedTx);
        utxoSet.add(wallet.getUtxos().get(0));
        assertThat(Transaction.isValid(signedTx, utxoSet)).isEqualTo(false);
    }

    @Test
    @DisplayName("test insufficient balance tx")
    void testInsufficientBalanceTransaction() {
        final Wallet recipient = new Wallet();

        Transaction unsignedTx = new Transaction(
                singletonList(wallet.getUtxos().get(0).getTxIn()),
                Arrays.asList(
                        new Output(pk, 30),
                        new Output(recipient.getPk(), 55)
                ),
                null);

        Transaction signedTx = wallet.sign(unsignedTx);
        utxoSet.add(wallet.getUtxos().get(0));
        assertThat(Transaction.isValid(signedTx, utxoSet)).isEqualTo(false);
    }
}
