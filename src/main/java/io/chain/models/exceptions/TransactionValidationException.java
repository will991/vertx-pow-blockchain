package io.chain.models.exceptions;

import io.chain.models.Transaction;

import static java.lang.String.format;
import static java.lang.System.lineSeparator;

public abstract class TransactionValidationException extends Exception {

    public TransactionValidationException(Transaction tx, String message) {
        super(
            format("Transaction [%s] Validation failed: %s%s%s%s",
                    tx.hash(), lineSeparator(),
                    message, lineSeparator(),
                    tx
            )
        );
    }
}
