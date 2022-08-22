package io.chain.models.exceptions;

import io.chain.models.Input;
import io.chain.models.Transaction;

import static java.lang.String.format;

public final class DoubleSpendException extends TransactionValidationException {

    public DoubleSpendException(Input in, Transaction tx) {
        super(tx, format("Input (%d) is not part of current spendable UTxO set.", tx.getInputs().lastIndexOf(in)));
    }
}
