package core.framework.internal.web.sys;

import core.framework.internal.kafka.KafkaMessage;
import core.framework.internal.kafka.MessageListener;
import core.framework.internal.kafka.MessageProcess;
import core.framework.internal.kafka.MessageProducer;
import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;
import core.framework.internal.log.Trace;
import core.framework.internal.log.filter.BytesLogParam;
import core.framework.internal.web.http.IPv4AccessControl;
import core.framework.json.JSON;
import core.framework.kafka.Message;
import core.framework.util.Strings;
import core.framework.web.Request;
import core.framework.web.Response;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import static core.framework.log.Markers.errorCode;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author neo
 */
public class KafkaController {
    private final Logger logger = LoggerFactory.getLogger(KafkaController.class);
    private final IPv4AccessControl accessControl = new IPv4AccessControl();
    public MessageProducer producer;
    public MessageListener listener;

    public Response publish(Request request) {
        accessControl.validate(request.clientIP());
        String topic = request.pathParam("topic");
        String key = request.pathParam("key");
        ActionLog actionLog = initAction(topic, key);
        byte[] body = request.body().orElseThrow(() -> new Error("body must not be null"));

        logger.warn(errorCode("MANUAL_OPERATION"), "publish message manually, topic={}", topic);   // log trace message, due to potential impact

        ProducerRecord<byte[], byte[]> record = record(topic, key, body, actionLog);
        producer.send(record);

        return Response.text(Strings.format("message published, topic={}, key={}, message={}", topic, key, new String(body, UTF_8)));
    }

    public Response handle(Request request) throws Exception {
        accessControl.validate(request.clientIP());
        String topic = request.pathParam("topic");
        String key = request.pathParam("key");
        ActionLog actionLog = initAction(topic, key);
        byte[] body = request.body().orElseThrow(() -> new Error("body must not be null"));

        logger.warn(errorCode("MANUAL_OPERATION"), "handle message manually, topic={}", topic);   // log trace message, due to potential impact

        @SuppressWarnings("unchecked")
        MessageProcess<Object> process = (MessageProcess<Object>) listener.processes.get(topic);
        @SuppressWarnings("unchecked")
        MessageProcess<Object> bulkProcess = (MessageProcess<Object>) listener.bulkProcesses.get(topic);
        if (process != null) {
            Object message = message(topic, key, body, process, actionLog);
            process.handler().handle(key, message);
        } else if (bulkProcess != null) {
            Object message = message(topic, key, body, bulkProcess, actionLog);
            bulkProcess.bulkHandler().handle(List.of(new Message<>(key, message)));
        } else {
            throw new Error("handler not found, topic=" + topic);
        }
        return Response.text(Strings.format("message handled, topic={}, key={}, message={}", topic, key, new String(body, UTF_8)));
    }

    private Object message(String topic, String key, byte[] body, MessageProcess<Object> process, ActionLog actionLog) throws IOException {
        Object message = process.reader.fromJSON(body);
        process.validator.validate(message, false);
        logger.debug("[message] topic={}, key={}, value={}", topic, key, new BytesLogParam(Strings.bytes(JSON.toJSON(message))));    // log converted message
        String handler = process.handler.getClass().getCanonicalName();
        actionLog.context.put("handler", List.of(handler));
        logger.debug("handler={}", handler);
        return message;
    }

    private ActionLog initAction(String topic, String key) {
        ActionLog actionLog = LogManager.CURRENT_ACTION_LOG.get();
        actionLog.context.put("topic", List.of(topic));
        actionLog.context.put("key", List.of(key));
        return actionLog;
    }

    ProducerRecord<byte[], byte[]> record(String topic, String key, byte[] body, ActionLog actionLog) {
        var record = new ProducerRecord<>(topic, Strings.bytes(key), body);
        Headers headers = record.headers();
        headers.add(KafkaMessage.HEADER_CLIENT, Strings.bytes(KafkaController.class.getSimpleName()));
        headers.add(KafkaMessage.HEADER_TRACE, Strings.bytes(Trace.CASCADE.name()));  // auto trace
        headers.add(KafkaMessage.HEADER_CORRELATION_ID, Strings.bytes(actionLog.correlationId()));
        headers.add(KafkaMessage.HEADER_REF_ID, Strings.bytes(actionLog.id));
        return record;
    }
}
