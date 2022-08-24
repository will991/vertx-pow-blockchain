package io.chain.models.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.bouncycastle.util.encoders.Hex;

import java.io.IOException;

public final class ByteArrayHexDeserializer extends StdDeserializer<byte[]> {

    protected ByteArrayHexDeserializer() { this(null); }


    protected ByteArrayHexDeserializer(Class<byte[]> t) {
        super(t);
    }

    @Override
    public byte[] deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        return Hex.decode(jsonParser.getText());
    }
}
