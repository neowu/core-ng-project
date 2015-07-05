package core.framework.api.module;

import core.framework.api.queue.MessageHandler;

/**
 * @author neo
 */
public interface MessageHandlerConfig {
    MessageHandlerConfig maxConcurrentHandlers(int maxConcurrentHandlers);

    <T> MessageHandlerConfig handle(Class<T> messageClass, MessageHandler<T> handler);
}
