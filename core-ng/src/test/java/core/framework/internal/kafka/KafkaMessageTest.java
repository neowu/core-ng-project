package core.framework.internal.kafka;

import core.framework.internal.log.Trace;
import core.framework.util.Strings;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaMessageTest {
    @Test
    void create() {
        var record = new ConsumerRecord<String, byte[]>("topic", 1, 0, "key", Strings.bytes("value"));
        record.headers().add(KafkaMessage.HEADER_TRACE, Strings.bytes(Trace.CURRENT.name()));
        var message = new KafkaMessage(record);

        assertThat(message.trace).isEqualTo(Trace.CURRENT);
        assertThat(message.correlationId).isNull();
    }

    @Test
    void header() {
        var record = new ConsumerRecord<String, byte[]>("topic", 1, 0, "key", Strings.bytes("value"));
        var message = new KafkaMessage(record);

        var headers = new RecordHeaders();
        headers.add("header", Strings.bytes("value"));
        assertThat(message.header(headers, "header")).isEqualTo("value");
        assertThat(message.header(headers, "nonExisted")).isNull();
    }
}
