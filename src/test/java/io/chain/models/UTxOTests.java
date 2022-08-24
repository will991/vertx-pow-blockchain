package io.chain.models;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UTxO Model Tests")
public final class UTxOTests {

    @Test
    @DisplayName("test UTxO equality")
    void testUTxOEquality() {
        final Wallet wallet = new Wallet();
        final Wallet wallet2 = new Wallet();
        final UTxO utxo1 = new UTxO(new Input("123".getBytes(), 0), new Output(wallet.getPk(), 50));
        final UTxO utxo2 = new UTxO(new Input("123".getBytes(), 0), new Output(wallet.getPk(), 50));
        assertThat(utxo1).isEqualTo(utxo2);

        final UTxO utxo3 = new UTxO(new Input("456".getBytes(), 0), new Output(wallet.getPk(), 50));
        assertThat(utxo3).isNotEqualTo(utxo2);

        final UTxO utxo4 = new UTxO(new Input("456".getBytes(), 0), new Output(wallet.getPk(), 10));
        assertThat(utxo3).isNotEqualTo(utxo4);

        final UTxO utxo5 = new UTxO(new Input("456".getBytes(), 0), new Output(wallet2.getPk(), 50));
        assertThat(utxo3).isNotEqualTo(utxo5);

        final UTxO utxo6 = new UTxO(new Input("456".getBytes(), 1), new Output(wallet.getPk(), 50));
        assertThat(utxo3).isNotEqualTo(utxo6);
    }

    @Test
    void testSerialization() {
        Wallet wallet = new Wallet();
        UTxO utxo = new UTxO(new Input("COINBASE".getBytes(), 0), new Output(wallet.getPk(), 10));
        final JsonObject jsonUtxo = new JsonObject(Json.encodePrettily(utxo));
        final JsonObject input = jsonUtxo.getJsonObject("txIn");
        final JsonObject output = jsonUtxo.getJsonObject("txOut");
        assertThat(new String(Hex.decode(input.getString("txHash")), StandardCharsets.UTF_8)).isEqualTo("COINBASE");
        assertThat(input.getInteger("index")).isEqualTo(0);
        assertThat(input.getValue("signature")).isNull();
        assertThat(output.getString("address")).isEqualTo(Hex.toHexString(wallet.getPk().toByteString().getBytes()));
        assertThat(output.getInteger("amount")).isEqualTo(10);
    }
}
