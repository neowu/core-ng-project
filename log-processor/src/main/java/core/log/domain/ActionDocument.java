package core.log.domain;

import core.framework.api.json.Property;
import core.framework.impl.log.message.PerformanceStatMessage;
import core.framework.search.Index;

import java.time.Instant;
import java.util.Map;

/**
 * @author neo
 */
@Index(index = "action", type = "action")
public class ActionDocument {
    @Property(name = "date")
    public Instant date;
    @Property(name = "app")
    public String app;
    @Property(name = "server_ip")
    public String serverIP;
    @Property(name = "id")
    public String id;
    @Property(name = "result")
    public String result;
    @Property(name = "ref_id")
    public String refId;
    @Property(name = "action")
    public String action;
    @Property(name = "error_code")
    public String errorCode;
    @Property(name = "error_message")
    public String errorMessage;
    @Property(name = "elapsed")
    public Long elapsed;
    @Property(name = "cpu_time")
    public Long cpuTime;
    @Property(name = "context")
    public Map<String, String> context;
    @Property(name = "stats")
    public Map<String, Double> stats;
    @Property(name = "perf_stats")
    public Map<String, PerformanceStatMessage> performanceStats;
}
