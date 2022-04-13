package core.framework.internal.kafka;

import core.framework.internal.json.JSONMapper;
import core.framework.internal.json.JSONReader;
import core.framework.internal.validate.Validator;
import core.framework.kafka.BulkMessageHandler;
import core.framework.kafka.MessageHandler;

/**
 * @author neo
 */
public class MessageProcess<T> {
    public final MessageHandler<T> handler;
    public final BulkMessageHandler<T> bulkHandler;
    public final JSONReader<T> reader;
    public final Validator<T> validator;

    MessageProcess(MessageHandler<T> handler, BulkMessageHandler<T> bulkHandler, Class<T> messageClass) {
        this.handler = handler;
        this.bulkHandler = bulkHandler;
        reader = JSONMapper.reader(messageClass);
        validator = Validator.of(messageClass);
    }
}
