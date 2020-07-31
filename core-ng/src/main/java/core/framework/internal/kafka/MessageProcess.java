package core.framework.internal.kafka;

import core.framework.internal.json.JSONMapper;
import core.framework.internal.json.JSONReader;
import core.framework.internal.validate.Validator;
import core.framework.kafka.BulkMessageHandler;
import core.framework.kafka.MessageHandler;

/**
 * @author neo
 */
class MessageProcess<T> {
    final MessageHandler<T> handler;
    final BulkMessageHandler<T> bulkHandler;
    final JSONReader<T> reader;
    final Validator<T> validator;

    MessageProcess(MessageHandler<T> handler, BulkMessageHandler<T> bulkHandler, Class<T> messageClass) {
        this.handler = handler;
        this.bulkHandler = bulkHandler;
        reader = JSONMapper.reader(messageClass);
        validator = Validator.of(messageClass);
    }
}
