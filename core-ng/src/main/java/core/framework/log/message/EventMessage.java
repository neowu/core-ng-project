package core.framework.log.message;

import core.framework.api.json.Property;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.Map;

/**
 * @author neo
 */
public class EventMessage {
    @Property(name = "id")
    public String id;
    @Property(name = "date")
    public Instant timestamp;
    @Property(name = "app")
    public String app;
    @Property(name = "client_timestamp")
    public Instant clientTimestamp;
    @Property(name = "result")
    public String result;
    @Property(name = "action")
    public String action;
    @Nullable
    @Property(name = "error_code")
    public String errorCode;
    @Property(name = "error_message")
    public String errorMessage;
    @Property(name = "elapsed")
    public Long elapsed;
    @Property(name = "context")
    public Map<String, String> context;
    @Property(name = "stats")
    public Map<String, Double> stats;
    @Property(name = "info")
    public Map<String, String> info;
}
