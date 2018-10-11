package core.framework.impl.kafka;

import core.framework.internal.json.JSONMapper;
import core.framework.kafka.BulkMessageHandler;
import core.framework.kafka.MessageHandler;

/**
 * @author neo
 */
class MessageProcess<T> {
    final MessageHandler<T> handler;
    final BulkMessageHandler<T> bulkHandler;
    final MessageValidator<T> validator;
    final JSONMapper<T> mapper;

    MessageProcess(MessageHandler<T> handler, BulkMessageHandler<T> bulkHandler, Class<T> messageClass) {
        this.handler = handler;
        this.bulkHandler = bulkHandler;
        validator = new MessageValidator<>(messageClass);
        mapper = new JSONMapper<>(messageClass);
    }
}
