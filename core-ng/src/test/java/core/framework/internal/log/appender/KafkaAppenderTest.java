package core.framework.internal.log.appender;

import core.framework.internal.kafka.KafkaURI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author neo
 */
class KafkaAppenderTest {
    private KafkaAppender appender;

    @BeforeEach
    void createKafkaAppender() {
        appender = new KafkaAppender(new KafkaURI("localhost"));
    }

    @Test
    void stop() {
        appender.stop(-1);
    }
}
