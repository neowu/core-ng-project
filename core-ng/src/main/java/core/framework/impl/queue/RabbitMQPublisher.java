package core.framework.impl.queue;

import com.rabbitmq.client.AMQP;
import core.framework.api.log.ActionLogContext;
import core.framework.api.queue.Message;
import core.framework.api.queue.MessagePublisher;
import core.framework.api.util.JSON;
import core.framework.api.util.Maps;

import java.util.Map;
import java.util.UUID;

/**
 * @author neo
 */
public class RabbitMQPublisher<T> implements MessagePublisher<T> {
    private final RabbitMQ rabbitMQ;
    private final RabbitMQEndpoint endpoint;
    private final String messageType;
    private final MessageValidator validator;

    public RabbitMQPublisher(RabbitMQ rabbitMQ, RabbitMQEndpoint endpoint, Class<T> messageClass, MessageValidator validator) {
        this.rabbitMQ = rabbitMQ;
        this.endpoint = endpoint;
        this.messageType = messageClass.getDeclaredAnnotation(Message.class).name();
        this.validator = validator;
    }

    @Override
    public void publish(T message) {
        publish(endpoint.exchange, endpoint.routingKey, message);
    }

    @Override
    public void publish(String queue, T message) {
        publish("", queue, message);
    }

    private void publish(String exchange, String routingKey, T message) {
        validator.validate(message);

        AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder()
            .messageId(UUID.randomUUID().toString())
            .type(messageType)
            .deliveryMode(2);   // persistent mode

        Map<String, Object> headers = Maps.newHashMap();
        headers.put(RabbitMQListener.HEADER_SENDER, Network.localHostName());
        linkContext(headers);
        builder.headers(headers);

        rabbitMQ.publish(exchange, routingKey, JSON.toJSON(message), builder.build());
    }

    private void linkContext(Map<String, Object> headers) {
        ActionLogContext.get(ActionLogContext.REQUEST_ID)
            .ifPresent(requestId -> headers.put(RabbitMQListener.HEADER_REQUEST_ID, requestId));

        ActionLogContext.get(ActionLogContext.TRACE)
            .ifPresent(trace -> {
                if ("true".equals(trace))
                    headers.put(RabbitMQListener.HEADER_TRACE, "true");
            });
    }
}
