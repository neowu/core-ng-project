package core.framework.kafka;

import java.io.Serial;

/**
 * @author neo
 */
public class KafkaException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -3982633342908753686L;

    public KafkaException(String message) {
        super(message);
    }
}
