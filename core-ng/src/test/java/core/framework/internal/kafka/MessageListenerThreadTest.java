package core.framework.internal.kafka;

import core.framework.internal.json.JSONMapper;
import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;
import core.framework.json.JSON;
import core.framework.kafka.BulkMessageHandler;
import core.framework.kafka.Message;
import core.framework.kafka.MessageHandler;
import core.framework.util.Strings;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.record.TimestampType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/**
 * @author neo
 */
class MessageListenerThreadTest {
    @Mock
    MessageHandler<TestMessage> messageHandler;
    @Mock
    BulkMessageHandler<TestMessage> bulkMessageHandler;
    private MessageListenerThread thread;

    @BeforeEach
    void createKafkaMessageListenerThread() {
        MockitoAnnotations.initMocks(this);
        thread = new MessageListenerThread("listener-thread-1", new MessageListener(null, null, new LogManager()));
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
    void messages() throws IOException {
        ConsumerRecord<byte[], byte[]> record = new ConsumerRecord<>("topic", 0, 1, Strings.bytes("key"), Strings.bytes("{}"));
        record.headers().add(MessageHeaders.HEADER_CLIENT, Strings.bytes("client"));
        record.headers().add(MessageHeaders.HEADER_REF_ID, Strings.bytes("refId"));
        record.headers().add(MessageHeaders.HEADER_CORRELATION_ID, Strings.bytes("correlationId"));
        var actionLog = new ActionLog(null);
        List<Message<TestMessage>> messages = thread.messages(List.of(record), actionLog, JSONMapper.reader(TestMessage.class));

        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).key).isEqualTo("key");
        assertThat(actionLog.context.get("key")).containsExactly("key");
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

    @Test
    void handle() throws Exception {
        var key = "key";
        var message = new TestMessage();
        message.stringField = "value";
        var record = new ConsumerRecord<>("topic", 0, 0, System.currentTimeMillis(), TimestampType.CREATE_TIME,
                -1, -1, -1, Strings.bytes(key), Strings.bytes(JSON.toJSON(message)));
        record.headers().add(MessageHeaders.HEADER_TRACE, Strings.bytes("true"));
        record.headers().add(MessageHeaders.HEADER_CLIENT, Strings.bytes("client"));
        thread.handle("topic", new MessageProcess<>(messageHandler, null, TestMessage.class), List.of(record), Duration.ofHours(1).toNanos());

        verify(messageHandler).handle(eq(key), argThat(value -> "value".equals(value.stringField)));
    }

    @Test
    void handleBulk() throws Exception {
        var key = "key";
        var message = new TestMessage();
        message.stringField = "value";
        var record = new ConsumerRecord<>("topic", 0, 0, System.currentTimeMillis(), TimestampType.CREATE_TIME,
                -1, -1, -1, Strings.bytes(key), Strings.bytes(JSON.toJSON(message)));
        record.headers().add(MessageHeaders.HEADER_CORRELATION_ID, Strings.bytes("correlationId"));
        record.headers().add(MessageHeaders.HEADER_REF_ID, Strings.bytes("refId"));
        thread.handleBulk("topic", new MessageProcess<>(null, bulkMessageHandler, TestMessage.class), List.of(record), Duration.ofHours(1).toNanos());

        verify(bulkMessageHandler).handle(argThat(value -> value.size() == 1
                && key.equals(value.get(0).key)
                && "value".equals(value.get(0).value.stringField)));
    }
}
