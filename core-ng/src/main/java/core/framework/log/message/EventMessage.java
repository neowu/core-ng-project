package core.framework.log.message;

import core.framework.api.json.Property;

import java.time.Instant;
import java.util.Map;

/**
 * @author neo
 */
public class EventMessage {
    @Property(name = "id")
    public String id;
    @Property(name = "date")
    public Instant date;
    @Property(name = "app")
    public String app;
    @Property(name = "received_time")
    public Instant receivedTime;
    @Property(name = "result")
    public String result;
    @Property(name = "action")
    public String action;
    @Property(name = "error_code")
    public String errorCode;
    @Property(name = "error_message")
    public String errorMessage;
    @Property(name = "context")
    public Map<String, String> context;
    @Property(name = "stats")
    public Map<String, Double> stats;
    @Property(name = "info")
    public Map<String, String> info;
    @Property(name = "elapsed")
    public Long elapsed;
}
