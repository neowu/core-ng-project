package core.framework.impl.kafka;

import core.framework.impl.json.JSONMapper;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

/**
 * @author neo
 */
public class KafkaMessageSerializer implements Serializer<KafkaMessage> {
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public byte[] serialize(String topic, KafkaMessage data) {
        byte[] headers = JSONMapper.toJSON(data.headers);

        int headerLength = headers.length;
        byte[] message = new byte[2 + 4 + headerLength + data.body.length];  // message format ["v1", lengthOfHeaders, headersBytes, bodyByes]

        message[0] = (byte) 'v';
        message[1] = (byte) '1';
        message[2] = (byte) (headerLength >> 24);
        message[3] = (byte) (headerLength >> 16);
        message[4] = (byte) (headerLength >> 8);
        message[5] = (byte) headerLength;

        System.arraycopy(headers, 0, message, 2 + 4, headerLength);
        System.arraycopy(data.body, 0, message, 2 + 4 + headerLength, data.body.length);

        return message;
    }

    @Override
    public void close() {

    }
}
