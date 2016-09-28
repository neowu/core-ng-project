package core.framework.impl.kafka;

import core.framework.api.util.Exceptions;
import core.framework.api.util.Maps;
import core.framework.api.util.Types;
import core.framework.impl.json.JSONMapper;
import core.framework.impl.json.JSONReader;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

/**
 * @author neo
 */
public class KafkaMessageDeserializer implements Deserializer<KafkaMessage> {
    private final Map<String, JSONReader> readers = Maps.newHashMap();

    public KafkaMessageDeserializer(Map<String, Class> typeMappings) {
        typeMappings.forEach((type, instanceClass) -> {
            readers.put(type, JSONReader.of(instanceClass));
        });
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public KafkaMessage deserialize(String topic, byte[] data) {
        if (data[0] != (byte) 'v' || data[1] != (byte) '1') throw Exceptions.error("unknown message format, message={}", new String(data));
        int headerLength = data[2] << 24 | (data[3] & 0xFF) << 16 | (data[4] & 0xFF) << 8 | (data[5] & 0xFF);
        Map<String, String> headers = JSONMapper.fromJSON(Types.map(String.class, String.class), data, 6, headerLength);
        String type = headers.get("type");
        JSONReader reader = readers.get(type);
        int bodyLength = data.length - 2 - 4 - headerLength;
        Object body = reader.fromJSON(data, 2 + 4 + headerLength, bodyLength);
        KafkaMessage message = new KafkaMessage();
        message.headers = headers;
//        message.body = body;
        return message;
    }

    @Override
    public void close() {

    }
}
