package core.framework.web.sse;

import java.util.List;

public interface ServerSentEventContext<T> {
    List<Channel<T>> all();

    List<Channel<T>> group(String name);
}
