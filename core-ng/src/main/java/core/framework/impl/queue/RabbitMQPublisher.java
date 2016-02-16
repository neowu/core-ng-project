package core.framework.impl.queue;

import com.rabbitmq.client.AMQP;
import core.framework.api.queue.Message;
import core.framework.api.queue.MessagePublisher;
import core.framework.api.util.Maps;
import core.framework.api.util.Network;
import core.framework.impl.json.JSONWriter;
import core.framework.impl.log.ActionLog;
import core.framework.impl.log.LogManager;

import java.util.Map;

/**
 * @author neo
 */
public class RabbitMQPublisher<T> implements MessagePublisher<T> {
    private final RabbitMQ rabbitMQ;
    private final String exchange;
    private final String routingKey;
    private final String messageType;
    private final MessageValidator validator;
    private final LogManager logManager;
    private final JSONWriter<T> writer;

    public RabbitMQPublisher(RabbitMQ rabbitMQ, String exchange, String routingKey, Class<T> messageClass, MessageValidator validator, LogManager logManager) {
        this.rabbitMQ = rabbitMQ;
        this.exchange = exchange;
        this.routingKey = routingKey;
        this.messageType = messageClass.getDeclaredAnnotation(Message.class).name();
        this.validator = validator;
        this.logManager = logManager;
        writer = JSONWriter.of(messageClass);
    }

    @Override
    public void publish(T message) {
        publish(exchange, routingKey, message);
    }

    @Override
    public void publish(String exchange, String routingKey, T message) {
        validator.validate(message);

        Map<String, Object> headers = Maps.newHashMap();
        headers.put(RabbitMQListener.HEADER_CLIENT_IP, Network.localHostAddress());

        AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder()
            .type(messageType)
            .deliveryMode(2)   // persistent mode
            .appId(logManager.appName)
            .headers(headers);

        linkContext(builder, headers);

        rabbitMQ.publish(exchange, routingKey, writer.toJSON(message), builder.build());
    }

    private void linkContext(AMQP.BasicProperties.Builder builder, Map<String, Object> headers) {
        ActionLog actionLog = logManager.currentActionLog();
        if (actionLog == null) return;

        builder.correlationId(actionLog.refId());
        if (actionLog.trace) headers.put(RabbitMQListener.HEADER_TRACE, "true");
    }
}
