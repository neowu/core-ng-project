package core.framework.internal.kafka;

import core.framework.internal.json.JSONMapper;
import core.framework.internal.json.JSONReader;
import core.framework.internal.log.PerformanceWarning;
import core.framework.internal.validate.Validator;
import core.framework.kafka.BulkMessageHandler;
import core.framework.kafka.MessageHandler;
import core.framework.log.IOWarning;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author neo
 */
public class MessageProcess<T> {
    public final MessageHandler<T> handler;
    public final BulkMessageHandler<T> bulkHandler;
    public final JSONReader<T> reader;
    public final Validator<T> validator;
    public final PerformanceWarning[] warnings;

    MessageProcess(MessageHandler<T> handler, BulkMessageHandler<T> bulkHandler, Class<T> messageClass) {
        this.handler = handler;
        this.bulkHandler = bulkHandler;
        reader = JSONMapper.reader(messageClass);
        validator = Validator.of(messageClass);
        try {
            Method targetMethod;
            if (handler != null) {
                targetMethod = handler.getClass().getMethod("handle", String.class, Object.class);
            } else {
                targetMethod = bulkHandler.getClass().getMethod("handle", List.class);
            }
            warnings = PerformanceWarning.of(targetMethod.getDeclaredAnnotationsByType(IOWarning.class));
        } catch (NoSuchMethodException e) {
            throw new Error(e);
        }
    }
}
