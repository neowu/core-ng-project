package core.framework.api.module;

import core.framework.api.queue.MessageHandler;

/**
 * @author neo
 */
public interface MessageHandlerConfig {
    <T> MessageHandlerConfig handle(Class<T> messageClass, MessageHandler<T> handler);

    MessageHandlerConfig maxConcurrentHandlers(int maxConcurrentHandlers);
}
