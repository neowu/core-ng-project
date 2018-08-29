package core.framework.impl.web.management;

import core.framework.impl.kafka.MessageHeaders;
import core.framework.impl.kafka.MessageProducer;
import core.framework.impl.log.filter.BytesParam;
import core.framework.impl.web.http.IPAccessControl;
import core.framework.log.ActionLogContext;
import core.framework.log.Markers;
import core.framework.util.Network;
import core.framework.util.Strings;
import core.framework.web.Request;
import core.framework.web.Response;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author neo
 */
public class KafkaController {
    private final Logger logger = LoggerFactory.getLogger(KafkaController.class);
    private final IPAccessControl accessControl = new IPAccessControl();
    private final MessageProducer producer;

    public KafkaController(MessageProducer producer) {
        this.producer = producer;
    }

    public Response publish(Request request) {
        accessControl.validate(request.clientIP());
        String topic = request.pathParam("topic");
        String key = request.pathParam("key");
        byte[] body = request.body().orElseThrow(() -> new Error("body must not be null"));

        ProducerRecord<String, byte[]> record = record(topic, key, body);
        logger.warn(Markers.errorCode("MANUAL_OPERATION"), "publish message manually, topic={}", topic);   // log trace message, due to potential impact
        producer.send(record);

        return Response.text(Strings.format("message published, topic={}, key={}, message={}", topic, key, new BytesParam(body)));
    }

    ProducerRecord<String, byte[]> record(String topic, String key, byte[] body) {
        var record = new ProducerRecord<>(topic, key, body);
        Headers headers = record.headers();
        headers.add(MessageHeaders.HEADER_CLIENT_IP, Strings.bytes(Network.localHostAddress()));
        headers.add(MessageHeaders.HEADER_CLIENT, Strings.bytes(KafkaController.class.getSimpleName()));
        String actionId = ActionLogContext.id();
        if (actionId != null) headers.add(MessageHeaders.HEADER_REF_ID, Strings.bytes(actionId));
        headers.add(MessageHeaders.HEADER_TRACE, Strings.bytes("true"));  // auto trace
        return record;
    }
}
