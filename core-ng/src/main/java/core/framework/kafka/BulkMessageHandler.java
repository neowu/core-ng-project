package core.framework.kafka;

import java.util.List;

/**
 * @author neo
 */
@FunctionalInterface
public interface BulkMessageHandler<T> {
    void handle(List<Message<T>> messages) throws Exception;
}
