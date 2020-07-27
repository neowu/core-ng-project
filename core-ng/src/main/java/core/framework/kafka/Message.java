package core.framework.kafka;

import javax.annotation.Nullable;

/**
 * @author neo
 */
public final class Message<T> {
    @Nullable
    public final String key;
    public final T value;

    public Message(@Nullable String key, T value) {
        this.key = key;
        this.value = value;
    }
}
