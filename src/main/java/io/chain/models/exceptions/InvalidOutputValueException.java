package io.chain.models.exceptions;

import io.chain.models.Output;
import io.chain.models.Transaction;

import static java.lang.String.format;

public final class InvalidOutputValueException extends TransactionValidationException {

    public InvalidOutputValueException(Output output, Transaction tx) {
        super(tx, format("Output (%d) value must be >= 0 but actual is: %d",
                tx.getOutputs().lastIndexOf(output),
                output.getAmount()));
    }
}
