package core.framework.internal.kafka;

import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.utils.Utils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class KeyDeserializer implements Deserializer<String> {
    @Override
    public String deserialize(String topic, byte[] data) {
        // this is not used if deserialize(String topic, Headers headers, ByteBuffer data) is used
        // refer to KIP-863: Reduce CompletedFetch#parseRecord() memory copy
        throw new Error("unexpected flow");
    }

    @Override
    public String deserialize(String topic, Headers headers, ByteBuffer data) {
        if (data == null) return null;

        if (data.hasArray()) {
            return new String(data.array(), data.position() + data.arrayOffset(), data.remaining(), StandardCharsets.UTF_8);
        }

        return new String(Utils.toArray(data), StandardCharsets.UTF_8);
    }
}
