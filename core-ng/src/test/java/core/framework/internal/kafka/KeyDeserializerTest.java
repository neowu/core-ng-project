package core.framework.internal.kafka;

import core.framework.util.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KeyDeserializerTest {
    private KeyDeserializer deserializer;

    @BeforeEach
    void createKeyDeserializer() {
        deserializer = new KeyDeserializer();
    }

    @Test
    void deserialize() {
        assertThat(deserializer.deserialize(null, null, (ByteBuffer) null)).isNull();

        String key = deserializer.deserialize(null, null, ByteBuffer.wrap(Strings.bytes("key1")));
        assertThat(key).isEqualTo("key1");

        var buffer = ByteBuffer.allocate(10);
        buffer.put(Strings.bytes("key2"));
        buffer.flip();
        key = deserializer.deserialize(null, null, buffer);
        assertThat(key).isEqualTo("key2");
    }

    @Test
    void deprecatedDeserialize() {
        assertThatThrownBy(() -> deserializer.deserialize(null, Strings.bytes("key")))
            .isInstanceOf(Error.class);
    }
}
