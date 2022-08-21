package io.chain.models;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("testing Output model")
public final class OutputTests {

    @Test
    @DisplayName("test Output equality and hashing")
    void testOutputEquality() {
        final Wallet wallet = new Wallet();
        final Output out1 = new Output(wallet.getPk(), 50);
        final Output out2 = new Output(wallet.getPk(), 50);
        Assertions.assertThat(out1).isEqualTo(out2);
        Assertions.assertThat(out1.hashCode()).isEqualTo(out2.hashCode());

        final Output out3 = new Output(wallet.getPk(), 5);
        Assertions.assertThat(out3).isNotEqualTo(out2);
        Assertions.assertThat(out3.hashCode()).isNotEqualTo(out2.hashCode());

        final Output out4 = new Output(new Wallet().getPk(), 5);
        Assertions.assertThat(out1).isNotEqualTo(out4);
        Assertions.assertThat(out1.hashCode()).isNotEqualTo(out4.hashCode());
        Assertions.assertThat(out3).isNotEqualTo(out4);
        Assertions.assertThat(out3.hashCode()).isNotEqualTo(out4.hashCode());
    }
}
