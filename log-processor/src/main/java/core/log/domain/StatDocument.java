package core.log.domain;

import core.framework.api.json.Property;
import core.framework.search.Index;

import java.time.Instant;
import java.util.Map;

/**
 * @author neo
 */
@Index(index = "stat", type = "stat")
public class StatDocument {
    @Property(name = "date")
    public Instant date;
    @Property(name = "app")
    public String app;
    @Property(name = "server_ip")
    public String serverIP;
    @Property(name = "stats")
    public Map<String, Double> stats;
}
