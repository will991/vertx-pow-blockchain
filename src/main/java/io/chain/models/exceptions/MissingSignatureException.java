package io.chain.models.exceptions;

import com.starkbank.ellipticcurve.PublicKey;
import io.chain.models.Transaction;
import org.bouncycastle.util.encoders.Hex;

import static java.lang.String.format;

public final class MissingSignatureException extends TransactionValidationException {

    public MissingSignatureException(PublicKey pk, Transaction tx) {
        super(tx, format("Missing signature of public key: %s", Hex.toHexString(pk.toByteString().getBytes())));
    }
}
