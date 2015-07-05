package core.framework.impl.queue;

import core.framework.api.util.Exceptions;

/**
 * @author neo
 */
public class RabbitMQEndpoint {
    public final String exchange;
    public final String routingKey;

    public RabbitMQEndpoint(String destinationURI) {
        if (destinationURI.startsWith("rabbitmq://exchange/")) {
            exchange = destinationURI.substring(20);
            routingKey = "";
        } else if (destinationURI.startsWith("rabbitmq://queue/")) {
            exchange = "";
            routingKey = destinationURI.substring(17);
        } else {
            throw Exceptions.error("unsupported rabbitmq uri, uri={}", destinationURI);
        }
    }
}
