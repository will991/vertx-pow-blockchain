package io.chain.models;

import com.starkbank.ellipticcurve.PrivateKey;
import com.starkbank.ellipticcurve.PublicKey;
import io.chain.models.exceptions.DoubleSpendException;
import io.chain.models.exceptions.MissingSignatureException;
import io.chain.models.exceptions.TransactionValidationException;
import io.chain.models.exceptions.UnbalancedTransactionException;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public final class TransactionTests {

    private final PrivateKey sk = new PrivateKey();
    private final PublicKey pk = sk.publicKey();
    private Wallet wallet;
    private UTxOSet utxoSet;
    private Transaction unsignedTx;

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
                singletonList(wallet.getUTxOs().get(0).getTxIn()),
                Arrays.asList(
                    new Output(pk, 30),
                    new Output(recipient.getPk(), 20)
                ),
                null);

        Transaction signedTx = wallet.sign(unsignedTx);
        utxoSet.add(wallet.getUTxOs().get(0));
        assertThat(Transaction.isValid(signedTx, utxoSet)).isEqualTo(true);

        try {
            Transaction.validate(signedTx, utxoSet);
        } catch (TransactionValidationException e) {
            fail("No Transaction validation exception expected", e);
        }

        utxoSet.process(Arrays.asList(signedTx));
        assertThat(utxoSet.contains(wallet.getUTxOs().get(0))).isEqualTo(false);
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
                singletonList(wallet.getUTxOs().get(0).getTxIn()),
                Arrays.asList(
                    new Output(pk, 30),
                    new Output(recipient.getPk(), 20)
                ),
                null);

        Transaction signedTx = wallet.sign(unsignedTx);
        assertThat(Transaction.isValid(signedTx, utxoSet)).isEqualTo(false);

        try {
            Transaction.validate(signedTx, utxoSet);
            fail("Expected input to not be spendable");
        } catch (DoubleSpendException e) {
            /* expected */
        } catch (TransactionValidationException e) {
            fail("No other validation exception expected", e);
        }
    }

    @Test
    @DisplayName("test invalid signature")
    void testInvalidSignature() {
        final Wallet recipient = new Wallet();

        Transaction unsignedTx = new Transaction(
                Arrays.asList(
                    wallet.getUTxOs().get(0).getTxIn(),
                    wallet.getUTxOs().get(0).getTxIn()
                ),
                Arrays.asList(
                        new Output(pk, 30),
                        new Output(recipient.getPk(), 20)
                ),
                null);

        Transaction signedTx = recipient.sign(unsignedTx);
        utxoSet.add(wallet.getUTxOs().get(0));
        assertThat(Transaction.isValid(signedTx, utxoSet)).isEqualTo(false);

        try {
            Transaction.validate(signedTx, utxoSet);
            fail("Expected missing signature exception");
        } catch (MissingSignatureException e) {
            /* expected */
        } catch (TransactionValidationException e) {
            fail("No other validation exception expected", e);
        }
    }

    @Test
    @DisplayName("test missing signature")
    void testMissingSignature() {
        final Wallet recipient = new Wallet();

        Transaction unsignedTx = new Transaction(
                Collections.singletonList(wallet.getUTxOs().get(0).getTxIn()),
                Arrays.asList(
                    new Output(pk, 30),
                    new Output(recipient.getPk(), 20)
                ),
                null);

        utxoSet.add(wallet.getUTxOs().get(0));
        Transaction signedTx = wallet.sign(unsignedTx);
        wallet.getUTxOs().get(0).getTxIn().addSignature(null);
        assertThat(Transaction.isValid(signedTx, utxoSet)).isEqualTo(false);

        try {
            Transaction.validate(signedTx, utxoSet);
            fail("Expected missing signature exception");
        } catch (MissingSignatureException e) {
            /* expected */
        } catch (TransactionValidationException e) {
            fail("No other validation exception expected", e);
        }
    }

    @Test
    @DisplayName("test double spending within same tx")
    void testDoubleSpendingWithinSameTransaction() {
        final Wallet recipient = new Wallet();

        Transaction unsignedTx = new Transaction(
                Arrays.asList(
                    wallet.getUTxOs().get(0).getTxIn(),
                    wallet.getUTxOs().get(0).getTxIn()
                ),
                Arrays.asList(
                        new Output(pk, 30),
                        new Output(recipient.getPk(), 20)
                ),
                null);

        Transaction signedTx = wallet.sign(unsignedTx);
        utxoSet.add(wallet.getUTxOs().get(0));
        assertThat(Transaction.isValid(signedTx, utxoSet)).isEqualTo(false);

        try {
            Transaction.validate(signedTx, utxoSet);
            fail("Expected double spend exception");
        }  catch (DoubleSpendException e) {
            /* expected */
        } catch (TransactionValidationException e) {
            fail("No other validation exception expected", e);
        }
    }

    @Test
    @DisplayName("test insufficient balance tx")
    void testInsufficientBalanceTransaction() {
        final Wallet recipient = new Wallet();

        Transaction unsignedTx = new Transaction(
                singletonList(wallet.getUTxOs().get(0).getTxIn()),
                Arrays.asList(
                        new Output(pk, 30),
                        new Output(recipient.getPk(), 55)
                ),
                null);

        Transaction signedTx = wallet.sign(unsignedTx);
        utxoSet.add(wallet.getUTxOs().get(0));
        assertThat(Transaction.isValid(signedTx, utxoSet)).isEqualTo(false);

        try {
            Transaction.validate(signedTx, utxoSet);
            fail("Expected unbalanced transaction exception");
        }  catch (UnbalancedTransactionException e) {
            /* expected */
        } catch (TransactionValidationException e) {
            fail("No other validation exception expected", e);
        }
    }

    @Test
    @DisplayName("test reward transaction")
    void testRewardTransaction() {
        final Transaction rewardTx = Transaction.rewardTransaction(wallet);
        assertThat(rewardTx.getInputs().size()).isEqualTo(1);
        assertThat(rewardTx.getInputs().get(0).getTxHash()).isEqualTo("COINBASE".getBytes());
        assertThat(rewardTx.getOutputs().size()).isEqualTo(1);
        assertThat(
            Hex.toHexString(
                rewardTx.getOutputs().get(0).getAddress().toByteString().getBytes()
            )
        ).isEqualTo(Hex.toHexString(wallet.getPk().toByteString().getBytes()));
        assertThat(rewardTx.getOutputs().get(0).getAmount()).isEqualTo(Block.MINING_REWARD);
    }
}
