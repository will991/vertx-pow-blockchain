package io.chain.models.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.starkbank.ellipticcurve.Signature;

import java.io.IOException;

public final class SignatureHexSerializer extends StdSerializer<Signature> {

    protected SignatureHexSerializer() {
        this(null);
    }

    protected SignatureHexSerializer(Class<Signature> t) {
        super(t);
    }

    @Override
    public void serialize(Signature signature, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(signature.toBase64());
    }
}
