package core.framework.internal.log.appender;

import core.framework.internal.kafka.KafkaURI;
import core.framework.kafka.KafkaException;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

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
    void createProducer() {
        KafkaProducer<byte[], byte[]> producer = appender.createProducer(new KafkaURI("localhost"));
        assertThat(producer).isNotNull();
        producer.close(Duration.ZERO);
    }

    @Test
    void stop() {
        appender.stop(0);
    }

    @Test
    void onCompletion() {
        var callback = appender.new KafkaCallback();
        appender.records.add(new ProducerRecord<>("topic", new byte[0]));
        callback.onCompletion(null, new KafkaException("unexpected"));
        assertThat(appender.records).isEmpty();
    }

    @Test
    void initialize() {
        appender.initialize();
    }
}
