package io.chain.models.serialization;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.starkbank.ellipticcurve.Signature;
import com.starkbank.ellipticcurve.utils.ByteString;

import java.io.IOException;

public class SignatureHexDeserializer extends StdDeserializer<Signature> {

    protected SignatureHexDeserializer() { this(null); }

    protected SignatureHexDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Signature deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        return Signature.fromBase64(new ByteString(jsonParser.getText().getBytes()));
    }
}
