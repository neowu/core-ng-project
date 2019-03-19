package core.framework.internal.log.message;

import core.framework.api.json.Property;

import java.time.Instant;
import java.util.Map;

/**
 * @author neo
 */
public class EventMessage {
    @Property(name = "id")
    public String id;
    @Property(name = "timestamp")
    public Instant timestamp;
    @Property(name = "app")
    public String app;
    @Property(name = "result")
    public String result;
    @Property(name = "type")
    public String type;
    @Property(name = "error_message")
    public String errorMessage;
    @Property(name = "exception_stack_trace")
    public String exceptionStackTrace;
    @Property(name = "elapsed")
    public Long elapsed;
    @Property(name = "collect_time")
    public Instant collectTime;
    @Property(name = "context")
    public Map<String, String> context;
}
