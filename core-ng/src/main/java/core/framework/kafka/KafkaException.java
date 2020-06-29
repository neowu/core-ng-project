package core.framework.kafka;

/**
 * @author neo
 */
public class KafkaException extends RuntimeException {
    private static final long serialVersionUID = -3982633342908753686L;

    public KafkaException(String message) {
        super(message);
    }
}
