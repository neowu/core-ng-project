package core.framework.http;

import core.framework.log.ActionLogContext;
import core.framework.util.StopWatch;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * @author neo
 */
public final class EventSource implements AutoCloseable, Iterable<EventSource.Event> {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventSource.class);

    public final int statusCode;
    public final Map<String, String> headers;   // headers key is case-insensitive

    private final ResponseBody body;
    private final int requestBodyLength;
    private int responseBodyLength;
    private int events;
    private long elapsed;

    @Nullable
    private String lastType;    // for "event" field
    @Nullable
    private String lastId;      // for "id" field
    @Nullable
    private Event nextEvent;

    public EventSource(int statusCode, Map<String, String> headers, ResponseBody body, int requestBodyLength, long elapsed) {
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body;
        this.requestBodyLength = requestBodyLength;
        this.elapsed = elapsed;
    }

    @Override
    public void close() {
        LOGGER.debug("[sse] close sse connection");
        ActionLogContext.track("sse", elapsed, events, 0, responseBodyLength, requestBodyLength);
        body.close();
    }

    @Override
    public Iterator<Event> iterator() {
        return new EventIterator();
    }

    private @Nullable Event parseResponse(BufferedSource source) {
        var watch = new StopWatch();
        try {
            while (true) {
                String line = source.readUtf8Line();
                if (line == null) return null;

                if (line.isEmpty()) {
                    lastId = null;
                    continue;
                }
                LOGGER.debug("[sse] line={}", line);
                responseBodyLength += line.length();
                int index = line.indexOf(": ");
                if (index == -1) continue;

                String field = line.substring(0, index);
                switch (field) {
                    case "id":
                        lastId = line.substring(index + 2);
                        break;
                    case "event":
                        lastType = line.substring(index + 2);
                        break;
                    case "data":
                        String id = lastId;
                        lastId = null;
                        String type = lastType;
                        lastType = null;
                        events++;
                        return new Event(id, type, line.substring(index + 2));
                    default:    // ignore "retry" and other fields
                }
            }
        } catch (IOException e) {
            throw new HTTPClientException("failed to read sse response, error=" + e.getMessage(), "HTTP_REQUEST_FAILED", e);
        } finally {
            elapsed += watch.elapsed();
        }
    }

    public record Event(@Nullable String id, @Nullable String type, String data) {
    }

    private final class EventIterator implements Iterator<Event> {
        @Override
        public boolean hasNext() {
            if (nextEvent != null) return true;
            nextEvent = parseResponse(body.source());
            return nextEvent != null;
        }

        @Override
        public Event next() {
            if (nextEvent != null || hasNext()) {
                var event = nextEvent;
                nextEvent = null;
                return event;
            } else {
                throw new NoSuchElementException();
            }
        }
    }
}
