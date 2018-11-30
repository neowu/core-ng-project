package core.framework.internal.log.appender;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author neo
 */
class KafkaAppenderTest {
    private KafkaAppender appender;

    @BeforeEach
    void createKafkaAppender() {
        appender = new KafkaAppender("localhost:9092");
    }

    @Test
    void stop() {
        appender.stop(-1);
    }
}
