package core.framework.web.sse;

import javax.annotation.Nullable;

public interface Channel<T> {
    // return true if event is queued, return false if channel is closed
    boolean send(String id, T event);

    default boolean send(T event) {
        return send(null, event);
    }

    Context context();

    // gracefully close, queue "end exchange" into io thread
    void close();

    void join(String group);

    void leave(String group);

    interface Context {
        @Nullable
        Object get(String key); // channel is stateful, context key usually be static put onConnect, so in most of the cases get(key) expects result, that's why here is designed to return Object, not Optional<T>

        void put(String key, @Nullable Object value);
    }
}
