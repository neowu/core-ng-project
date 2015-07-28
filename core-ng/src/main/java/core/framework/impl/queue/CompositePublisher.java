package core.framework.impl.queue;

import core.framework.api.queue.MessagePublisher;

import java.util.List;

/**
 * @author neo
 */
public class CompositePublisher<T> implements MessagePublisher<T> {
    private final List<MessagePublisher<T>> publishers;

    public CompositePublisher(List<MessagePublisher<T>> publishers) {
        this.publishers = publishers;
    }

    @Override
    public void publish(T message) {
        publishers.forEach(publisher -> publisher.publish(message));
    }

    @Override
    public void publish(String routingKey, T message) {
        publishers.forEach(publisher -> publisher.publish(routingKey, message));
    }
}
