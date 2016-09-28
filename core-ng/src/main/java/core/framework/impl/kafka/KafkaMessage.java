package core.framework.impl.kafka;

import java.util.Map;

/**
 * @author neo
 */
public class KafkaMessage {
    public Map<String, String> headers;
    public byte[] body;
}
