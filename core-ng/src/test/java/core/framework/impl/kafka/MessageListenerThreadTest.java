package core.framework.impl.kafka;

import core.framework.util.Strings;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

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
}
