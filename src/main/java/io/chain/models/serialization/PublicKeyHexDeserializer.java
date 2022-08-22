package io.chain.models.serialization;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.starkbank.ellipticcurve.PublicKey;
import com.starkbank.ellipticcurve.utils.ByteString;
import com.syntifi.crypto.key.encdec.Hex;

import java.io.IOException;

public class PublicKeyHexDeserializer extends StdDeserializer<PublicKey> {

    protected PublicKeyHexDeserializer() { this(null); }

    protected PublicKeyHexDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public PublicKey deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        return PublicKey.fromString(new ByteString(Hex.decode(jsonParser.getText())));
    }
}
