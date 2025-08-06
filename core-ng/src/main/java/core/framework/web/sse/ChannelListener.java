package core.framework.web.sse;

import core.framework.web.Request;
import org.jspecify.annotations.Nullable;

public interface ChannelListener<T> {
    void onConnect(Request request, Channel<T> channel, @Nullable String lastEventId);

    default void onClose(Channel<T> channel) {
    }
}
