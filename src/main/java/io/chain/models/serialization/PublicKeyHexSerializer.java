package io.chain.models.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.starkbank.ellipticcurve.PublicKey;
import org.bouncycastle.util.encoders.Hex;

import java.io.IOException;

public class PublicKeyHexSerializer extends StdSerializer<PublicKey> {

    protected PublicKeyHexSerializer() { this(null); }
    protected PublicKeyHexSerializer(Class<PublicKey> t) {
        super(t);
    }

    @Override
    public void serialize(PublicKey publicKey, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(Hex.toHexString(publicKey.toByteString().getBytes()));
    }
}
