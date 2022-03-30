package core.framework.internal.log.appender;

import core.framework.internal.kafka.KafkaURI;
import core.framework.kafka.KafkaException;
import core.framework.log.message.ActionLogMessage;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;

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

    @Test
    void resolveURI() {
        assertThat(appender.resolveURI(new KafkaURI("localhost"))).isTrue();
        assertThat(appender.resolveURI(new KafkaURI("notExistedHost"))).isFalse();
    }

    @Test
    void truncateContext() {
        var message = new ActionLogMessage();
        message.context = new HashMap<>();
        message.context.put("key1", List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"));
        message.context.put("key2", List.of("1"));
        String traceLog = "1234567890".repeat(10);
        message.traceLog = traceLog;
        appender.truncate(message, 20, 5000);

        assertThat(message.context).containsOnlyKeys("key2");
        assertThat(message.traceLog).isEqualTo(traceLog);
    }

    @Test
    void truncateTrace() {
        var message = new ActionLogMessage();
        message.context = new HashMap<>();
        message.context.put("key2", List.of("1"));
        message.traceLog = "1234567890".repeat(50);
        appender.truncate(message, 200, 100);

        assertThat(message.context).containsOnlyKeys("key2");
        assertThat(message.traceLog)
                .hasSize(370)   // last "warning" text has 70 chars, so only trimmed 130 chars, though overflow is 200
                .endsWith("...(hard trace limit reached, please check console log for full trace)");
    }
}
