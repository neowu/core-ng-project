package core.framework.util;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class EncodingsTest {
    @Test
    void base64() {
        assertThat(Encodings.base64("")).isEqualTo("");
        // from http://en.wikipedia.org/wiki/Base64
        assertThat(Encodings.base64("leasure.")).isEqualTo("bGVhc3VyZS4=");
    }

    @Test
    void decodeBase64() {
        // from http://en.wikipedia.org/wiki/Base64
        assertThat(new String(Encodings.decodeBase64("bGVhc3VyZS4="), StandardCharsets.UTF_8)).isEqualTo("leasure.");
    }

    @Test
    void base64URLSafe() {
        byte[] bytes = new byte[256];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) i;
        }
        String encodedMessage = Encodings.base64URLSafe(bytes);
        assertThat(Encodings.decodeBase64URLSafe(encodedMessage)).containsExactly(bytes);
    }

    @Test
    void uriComponent() {
        assertThat(Encodings.uriComponent("✓")).as("encode utf-8").isEqualTo("%E2%9C%93");
        assertThat(Encodings.uriComponent("a b")).isEqualTo("a%20b");
        assertThat(Encodings.uriComponent("a+b")).isEqualTo("a%2Bb");
        assertThat(Encodings.uriComponent("a=b")).isEqualTo("a%3Db");
        assertThat(Encodings.uriComponent("a?b")).isEqualTo("a%3Fb");
        assertThat(Encodings.uriComponent("a/b")).isEqualTo("a%2Fb");
        assertThat(Encodings.uriComponent("a&b")).isEqualTo("a%26b");
        assertThat(Encodings.uriComponent("a%b")).isEqualTo("a%25b");
    }

    @Test
    void decodeURIComponent() {
        assertThat(Encodings.decodeURIComponent("%E2%9C%93")).as("decode utf-8").isEqualTo("✓");
        assertThat(Encodings.decodeURIComponent("a%20b")).isEqualTo("a b");
        assertThat(Encodings.decodeURIComponent("a+b")).isEqualTo("a+b");
        assertThat(Encodings.decodeURIComponent("a=b")).isEqualTo("a=b");
        assertThat(Encodings.decodeURIComponent("a%3Fb")).isEqualTo("a?b");
        assertThat(Encodings.decodeURIComponent("a%3fb")).isEqualTo("a?b");
        assertThat(Encodings.decodeURIComponent("a%2Fb")).isEqualTo("a/b");
        assertThat(Encodings.decodeURIComponent("a&b")).isEqualTo("a&b");
        assertThat(Encodings.decodeURIComponent("a%25b")).isEqualTo("a%b");
    }

    @Test
    void hex() {
        assertThat(Encodings.hex(new byte[]{0, 1, 0xf})).isEqualTo("00010F");
        assertThat(Encodings.hex(new byte[]{-128, -1, 127})).isEqualTo("80FF7F");
        assertThat(Encodings.hex(new byte[0])).isEqualTo("");
    }
}
