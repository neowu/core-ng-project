package core.framework.web.sse;

import java.util.List;

public interface ServerSentEventContext<T> {
    List<ServerSentEventChannel<T>> all();

    List<ServerSentEventChannel<T>> group(String name);
}
