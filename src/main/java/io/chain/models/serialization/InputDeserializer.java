package io.chain.models.serialization;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.starkbank.ellipticcurve.Signature;
import com.starkbank.ellipticcurve.utils.ByteString;
import io.chain.models.Input;
import org.bouncycastle.util.encoders.Hex;

import java.io.IOException;

public class InputDeserializer extends StdDeserializer<Input> {

    protected InputDeserializer() {
        this(null);
    }

    protected InputDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Input deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        final JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        final byte[] txHash = Hex.decode(node.get("txHash").textValue());
        System.out.println("TxHash: " + txHash);
        final int txIdx = (int) node.get("index").numberValue();
        final Signature sig = Signature.fromBase64(new ByteString(node.get("signature").textValue().getBytes()));
        return new Input(txHash, txIdx, sig);
    }
}
