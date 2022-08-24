package io.chain.models.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.starkbank.ellipticcurve.PublicKey;
import com.starkbank.ellipticcurve.Signature;
import com.starkbank.ellipticcurve.utils.ByteString;
import io.chain.models.Input;
import io.chain.models.Output;
import io.chain.models.Transaction;
import org.bouncycastle.util.encoders.Hex;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TransactionDeserializer extends StdDeserializer<Transaction> {

    protected TransactionDeserializer() { this(null); }

    protected TransactionDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Transaction deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        final List<Input> ins = new ArrayList<>();
        final ArrayNode inputs = (ArrayNode) node.get("inputs");
        for (int i = 0; i < inputs.size(); i++) {
            final JsonNode n = inputs.get(i);
            final byte[] txHash = new String(Hex.decode(n.get("txHash").textValue()), StandardCharsets.UTF_8).getBytes();
            final int txIdx = (int) n.get("index").numberValue();
            final Signature sig = Signature.fromBase64(new ByteString(n.get("signature").textValue().getBytes()));
            ins.add(new Input(txHash, txIdx, sig));
        }

        final List<Output> outs = new ArrayList<>();
        final ArrayNode outputs = (ArrayNode) node.get("outputs");
        for (int i = 0; i < outputs.size(); i++) {
            final JsonNode n = outputs.get(i);
            final PublicKey address = PublicKey.fromString(new ByteString(Hex.decode(n.get("address").textValue())));
            final int amount = (int) n.get("amount").numberValue();
            outs.add(new Output(address, amount));
        }

        final Transaction transaction;
        if (node.has("data")) {
            transaction = new Transaction(ins, outs, node.get("data").asText().getBytes());
        } else {
            transaction = new Transaction(ins, outs);
        }

        return transaction;
    }
}
