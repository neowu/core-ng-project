package core.framework.internal.kafka;

import core.framework.internal.json.JSONMapper;
import core.framework.internal.log.ActionLog;
import core.framework.kafka.Message;
import core.framework.util.Strings;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class MessageListenerThreadTest {
    private MessageListenerThread thread;

    @BeforeEach
    void createKafkaMessageListenerThread() {
        thread = new MessageListenerThread("listener-thread-1", null, new MessageListener(null, null, null));
    }

    @Test
    void longProcessThreshold() {
        assertThat(thread.longProcessThreshold(Duration.ofNanos(500).toNanos(), 1, 100)).isEqualTo(5);
        assertThat(thread.longProcessThreshold(Duration.ofNanos(500).toNanos(), 1, 1)).isEqualTo(500);
    }

    @Test
    void header() {
        var headers = new RecordHeaders();
        headers.add("header", Strings.bytes("value"));
        assertThat(thread.header(headers, "header")).isEqualTo("value");
        assertThat(thread.header(headers, "nonExisted")).isNull();
    }

    @Test
    void messages() {
        ConsumerRecord<byte[], byte[]> record = new ConsumerRecord<>("topic", 0, 1, Strings.bytes("key"), Strings.bytes("{}"));
        record.headers().add(MessageHeaders.HEADER_CLIENT, Strings.bytes("client"));
        record.headers().add(MessageHeaders.HEADER_REF_ID, Strings.bytes("refId"));
        record.headers().add(MessageHeaders.HEADER_CORRELATION_ID, Strings.bytes("correlationId"));
        var actionLog = new ActionLog(null);
        List<Message<TestMessage>> messages = thread.messages(List.of(record), actionLog, new JSONMapper<>(TestMessage.class));

        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).key).isEqualTo("key");
        assertThat(actionLog.clients).containsExactly("client");
        assertThat(actionLog.refIds).containsExactly("refId");
        assertThat(actionLog.correlationIds).containsExactly("correlationId");
    }

    @Test
    void checkConsumerLag() {
        thread.checkConsumerLag(99003, 60000);
    }

    @Test
    void checkSlowProcess() {
        thread.checkSlowProcess(Duration.ofSeconds(30).toNanos(), Duration.ofSeconds(25).toNanos());
    }

    @Test
    void key() {
        assertThat(thread.key(new ConsumerRecord<>("topic", 0, 0, null, null)))
                .isNull();

        assertThat(thread.key(new ConsumerRecord<>("topic", 0, 0, Strings.bytes("key"), null)))
                .isEqualTo("key");
    }
}
