package io.chain.models.exceptions;

import io.chain.models.Transaction;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

import static java.lang.String.format;
import static java.lang.System.lineSeparator;

public abstract class TransactionValidationException extends Exception {

    private Transaction tx;
    private String msg;

    public TransactionValidationException(Transaction tx, String message) {
        super(
            format("Transaction [%s] Validation failed: %s%s%s%s",
                    tx.hash(), lineSeparator(),
                    message, lineSeparator(),
                    tx
            )
        );
        this.tx = tx;
        this.msg = message;
    }

    public JsonObject toJson() {
        return new JsonObject().put("error", format("Transaction validation failed. %s", msg));
    }
}
