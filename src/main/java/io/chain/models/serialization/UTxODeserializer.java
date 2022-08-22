package io.chain.models.serialization;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.starkbank.ellipticcurve.PublicKey;
import com.starkbank.ellipticcurve.Signature;
import com.starkbank.ellipticcurve.utils.ByteString;
import io.chain.models.Input;
import io.chain.models.Output;
import io.chain.models.UTxO;
import org.bouncycastle.util.encoders.Hex;

import java.io.IOException;

public class UTxODeserializer extends StdDeserializer<UTxO> {

    protected UTxODeserializer() {
        this(null);
    }

    protected UTxODeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public UTxO deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        final JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        final JsonNode iNode = node.get("input");
        final byte[] txHash = Hex.decode(iNode.get("txHash").textValue());
        final int txIdx = (int) iNode.get("index").numberValue();
        final Signature sig = Signature.fromBase64(new ByteString(iNode.get("signature").textValue().getBytes()));
        final Input input = new Input(txHash, txIdx, sig);

        final JsonNode oNode = node.get("output");
        final PublicKey address = PublicKey.fromString(new ByteString(Hex.decode(oNode.get("address").textValue())));
        final int amount = (int) oNode.get("amount").numberValue();
        final Output output = new Output(address, amount);
        return new UTxO(input, output);
    }
}
