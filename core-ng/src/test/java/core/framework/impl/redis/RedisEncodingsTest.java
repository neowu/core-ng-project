package core.framework.impl.redis;

import core.framework.util.Strings;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class RedisEncodingsTest {
    @Test
    void encodeString() {
        assertThat(RedisEncodings.encode("value")).isEqualTo(Strings.bytes("value"));
    }

    @Test
    void encodeLong() {
        assertThat(RedisEncodings.encode(-1)).isEqualTo(Strings.bytes("-1"));
        assertThat(RedisEncodings.encode(9)).isEqualTo(Strings.bytes("9"));
        assertThat(RedisEncodings.encode(88)).isEqualTo(Strings.bytes("88"));
        assertThat(RedisEncodings.encode(777)).isEqualTo(Strings.bytes("777"));
        assertThat(RedisEncodings.encode(6666)).isEqualTo(Strings.bytes("6666"));
        assertThat(RedisEncodings.encode(55555)).isEqualTo(Strings.bytes("55555"));
        assertThat(RedisEncodings.encode(444444)).isEqualTo(Strings.bytes("444444"));
        assertThat(RedisEncodings.encode(3333333)).isEqualTo(Strings.bytes("3333333"));
        assertThat(RedisEncodings.encode(-1234567890)).isEqualTo(Strings.bytes("-1234567890"));

        assertThat(RedisEncodings.encode(5L)).isEqualTo(Strings.bytes("5"));
        assertThat(RedisEncodings.encode(-1234567890123456789L)).isEqualTo(Strings.bytes("-1234567890123456789"));
    }

    @Test
    void decode() {
        assertThat(RedisEncodings.decode(Strings.bytes("value"))).isEqualTo("value");
    }
}
