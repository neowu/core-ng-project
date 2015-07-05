package core.framework.impl.queue;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import core.framework.api.log.ActionLogContext;
import core.framework.api.queue.Message;
import core.framework.api.queue.MessagePublisher;
import core.framework.api.util.JSON;
import core.framework.api.util.Maps;
import core.framework.api.util.StopWatch;
import core.framework.api.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.UUID;

/**
 * @author neo
 */
public class RabbitMQPublisher<T> implements MessagePublisher<T> {
    private final Logger logger = LoggerFactory.getLogger(RabbitMQPublisher.class);
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
    public void reply(String queue, T message) {
        publish("", queue, message);
    }

    private void publish(String exchange, String routingKey, T message) {
        StopWatch watch = new StopWatch();
        Channel channel = null;
        String messageId = null;
        try {
            validator.validate(message);
            channel = rabbitMQ.channel();

            messageId = UUID.randomUUID().toString();

            AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder()
                .messageId(messageId)
                .type(messageType);

            Map<String, Object> headers = Maps.newHashMap();
            headers.put("sender", Network.localHostName());
            builder.headers(headers);

            linkContext(headers);

            channel.basicPublish(exchange, routingKey, builder.build(), Strings.bytes(JSON.toJSON(message)));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            rabbitMQ.closeChannel(channel);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("rabbitmq", elapsedTime);
            logger.debug("publish message, exchange={}, routingKey={}, messageId={}, elapsedTime={}", exchange, routingKey, messageId, elapsedTime);
        }
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
