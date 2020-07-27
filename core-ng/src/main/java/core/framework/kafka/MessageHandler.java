package core.framework.kafka;

import javax.annotation.Nullable;

/**
 * @author neo
 */
@FunctionalInterface
public interface MessageHandler<T> {
    void handle(@Nullable String key, T value) throws Exception;
}
