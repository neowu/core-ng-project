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
    @Property(name = "event_time")
    public Instant eventTime;
    @Property(name = "result")
    public String result;
    @Property(name = "action")
    public String action;
    @Property(name = "error_code")
    public String errorCode;
    @Property(name = "context")
    public Map<String, String> context;
    @Property(name = "info")
    public Map<String, String> info;
    @Property(name = "elapsed")
    public Long elapsed;
}
