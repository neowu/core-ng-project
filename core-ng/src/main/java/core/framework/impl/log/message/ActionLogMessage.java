package core.framework.impl.log.message;

import core.framework.api.json.Property;

import java.time.Instant;
import java.util.Map;

/**
 * @author neo
 */
public class ActionLogMessage {
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
    @Property(name = "trace_log")
    public String traceLog;
}
