package core.framework.api.log;

import core.framework.impl.log.ActionLogger;
import core.framework.impl.log.DefaultLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.StaticLoggerBinder;

import java.util.Optional;

/**
 * @author neo
 */
public final class ActionLogContext {
    public static final String REQUEST_ID = "requestId";
    public static final String ACTION = "action";
    public static final String TRACE = "trace";
    public static final String ERROR_MESSAGE = "errorMessage";
    public static final String EXCEPTION_CLASS = "exceptionClass";

    private static final Logger LOGGER = LoggerFactory.getLogger(ActionLogContext.class);

    public static Optional<String> get(String key) {
        ActionLogger actionLogger = actionLogger();
        return actionLogger.get(key);
    }

    public static void put(String key, Object value) {
        ActionLogger actionLogger = actionLogger();
        LOGGER.debug("[context] {}={}", key, value);
        actionLogger.put(key, value);
    }

    public static void track(String action, long elapsedTime) {
        ActionLogger actionLogger = actionLogger();
        actionLogger.track(action, elapsedTime);
    }

    private static ActionLogger actionLogger() {
        return ((DefaultLoggerFactory) StaticLoggerBinder.getSingleton().getLoggerFactory()).actionLogger;
    }
}
