package core.framework.internal.kafka;

import core.framework.internal.json.JSONMapper;
import core.framework.internal.json.JSONReader;
import core.framework.internal.log.PerformanceWarning;
import core.framework.internal.log.WarningContext;
import core.framework.internal.validate.Validator;
import core.framework.kafka.BulkMessageHandler;
import core.framework.kafka.MessageHandler;
import core.framework.log.IOWarning;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author neo
 */
public class MessageProcess<T> {
    public final Object handler;
    public final JSONReader<T> reader;
    public final Validator<T> validator;
    @Nullable
    public final PerformanceWarning[] warnings;

    MessageProcess(Object handler, Class<T> messageClass) {
        this.handler = handler;
        reader = JSONMapper.reader(messageClass);
        validator = Validator.of(messageClass);
        this.warnings = warnings(handler);
    }

    @SuppressWarnings("unchecked")
    public MessageHandler<T> handler() {
        return (MessageHandler<T>) handler;
    }

    @SuppressWarnings("unchecked")
    public BulkMessageHandler<T> bulkHandler() {
        return (BulkMessageHandler<T>) handler;
    }

    @Nullable
    private PerformanceWarning[] warnings(Object handler) {
        try {
            Method targetMethod;
            if (handler instanceof MessageHandler) {
                targetMethod = handler.getClass().getMethod("handle", String.class, Object.class);
            } else {
                targetMethod = handler.getClass().getMethod("handle", List.class);
            }
            return WarningContext.warnings(targetMethod.getDeclaredAnnotationsByType(IOWarning.class));
        } catch (NoSuchMethodException e) {
            throw new Error(e);
        }
    }
}
