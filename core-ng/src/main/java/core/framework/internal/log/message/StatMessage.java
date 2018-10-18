package core.framework.internal.log.message;

import core.framework.api.json.Property;

import java.time.Instant;
import java.util.Map;

/**
 * @author neo
 */
public class StatMessage {
    @Property(name = "id")
    public String id;
    @Property(name = "date")
    public Instant date;
    @Property(name = "app")
    public String app;
    @Property(name = "server_ip")
    public String serverIP;
    @Property(name = "stats")
    public Map<String, Double> stats;
}
