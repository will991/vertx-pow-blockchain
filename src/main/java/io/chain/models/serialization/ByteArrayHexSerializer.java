package io.chain.models.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.bouncycastle.util.encoders.Hex;

import java.io.IOException;

public final class ByteArrayHexSerializer extends StdSerializer<byte[]> {

    protected ByteArrayHexSerializer() { this(null); }

    protected ByteArrayHexSerializer(Class<byte[]> t) {
        super(t);
    }

    @Override
    public void serialize(byte[] bytes, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(Hex.toHexString(bytes));
    }
}
