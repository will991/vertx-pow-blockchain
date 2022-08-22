package io.chain.models.exceptions;

public final class InsufficientUTxOBalanceException extends Exception {

    public InsufficientUTxOBalanceException(int amount, int walletBalance) {
        super(String.format("The transaction amount (%d) spent exceeds the wallet's balance (%d)", amount, walletBalance));
    }
}
