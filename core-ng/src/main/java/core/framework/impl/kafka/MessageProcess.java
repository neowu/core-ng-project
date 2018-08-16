package core.framework.impl.kafka;

import core.framework.impl.json.JSONReader;
import core.framework.kafka.BulkMessageHandler;
import core.framework.kafka.MessageHandler;

/**
 * @author neo
 */
class MessageProcess<T> {
    final MessageHandler<T> handler;
    final BulkMessageHandler<T> bulkHandler;
    final MessageValidator<T> validator;
    final JSONReader<T> reader;

    MessageProcess(MessageHandler<T> handler, BulkMessageHandler<T> bulkHandler, JSONReader<T> reader, MessageValidator<T> validator) {
        this.handler = handler;
        this.bulkHandler = bulkHandler;
        this.validator = validator;
        this.reader = reader;
    }
}
