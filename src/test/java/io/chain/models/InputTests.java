package io.chain.models;

import com.starkbank.ellipticcurve.Ecdsa;
import com.starkbank.ellipticcurve.PrivateKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Input Model Tests")
public final class InputTests {

    private final Input in1 = new Input("123".getBytes(), 0);
    private final Input in2 = new Input("456".getBytes(), 0);

    @Test
    @DisplayName("test Input comparability")
    void testUTxOComparability() {
        assertThat(in1.compareTo(in2)).isEqualTo(-1);
        assertThat(in1.compareTo(new Input("123".getBytes(), 0))).isEqualTo(0);
        assertThat(in1.compareTo(new Input("123".getBytes(), 1))).isEqualTo(-1);
        assertThat(new Input("123".getBytes(), 1).compareTo(in1)).isEqualTo(1);
        assertThat(in2.compareTo(in1)).isEqualTo(1);
    }

    @Test
    @DisplayName("test Input copy")
    void testUTxOCopy() {
        byte[] in = "123".getBytes();
        final Input i = new Input(in, 0);
        in[1] = 0;
        assertThat(i.getTxHash()).isEqualTo("123".getBytes());
    }

    @Test
    @DisplayName("test equality")
    void testUTxOEquality() {
        final Input in3 = new Input("456".getBytes(), 0);
        assertThat(in3).isEqualTo(in2);
        assertThat(in3).isNotEqualTo(in1);

        final PrivateKey sk = new PrivateKey();
        in3.addSignature(Ecdsa.sign("", sk));
        assertThat(in3).isNotEqualTo(in2);
    }
}
