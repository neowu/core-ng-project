package core.framework.internal.kafka;

import core.framework.internal.log.Trace;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;

import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class KafkaMessage {
    public static final String HEADER_CORRELATION_ID = "correlationId";
    public static final String HEADER_TRACE = "trace";
    public static final String HEADER_CLIENT = "client";
    public static final String HEADER_REF_ID = "refId";

    final String key;
    final byte[] value;
    final Trace trace;
    final String correlationId;
    final String refId;
    final String client;
    final long timestamp;

    // one poll batch is small (~500) and short (~500ms), it doesn't expect many messages with same key (less than 3?)
    List<KafkaMessage> subsequent;

    KafkaMessage(ConsumerRecord<String, byte[]> record) {
        key = record.key();
        value = record.value();

        Headers headers = record.headers();
        refId = header(headers, HEADER_REF_ID);
        correlationId = header(headers, HEADER_CORRELATION_ID);
        client = header(headers, HEADER_CLIENT);
        String trace = header(headers, HEADER_TRACE);
        this.trace = trace == null ? null : Trace.parse(trace);

        timestamp = record.timestamp();
    }

    final String header(Headers headers, String key) {
        Header header = headers.lastHeader(key);
        if (header == null) return null;
        return new String(header.value(), UTF_8);
    }

    void addSubsequent(KafkaMessage message) {
        if (subsequent == null) subsequent = new ArrayList<>(5);
        subsequent.add(message);
    }
}
