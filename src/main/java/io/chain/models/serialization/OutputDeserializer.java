package io.chain.models.serialization;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.starkbank.ellipticcurve.PublicKey;
import com.starkbank.ellipticcurve.utils.ByteString;
import io.chain.models.Output;
import org.bouncycastle.util.encoders.Hex;

import java.io.IOException;

public class OutputDeserializer extends StdDeserializer<Output> {

    protected OutputDeserializer() {
        this(null);
    }

    protected OutputDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Output deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        final JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        final PublicKey address = PublicKey.fromString(new ByteString(Hex.decode(node.get("address").textValue())));
        final int amount = (int) node.get("amount").numberValue();
        return new Output(address, amount);
    }
}
