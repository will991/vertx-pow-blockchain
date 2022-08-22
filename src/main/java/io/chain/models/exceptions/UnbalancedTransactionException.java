package io.chain.models.exceptions;

import io.chain.models.Transaction;

import static java.lang.String.format;

public final class UnbalancedTransactionException extends TransactionValidationException {

    public UnbalancedTransactionException(int inSum, int outSum, Transaction tx) {
        super(tx, format("Input amount unequal output value: %d != %d", inSum, outSum));
    }
}
