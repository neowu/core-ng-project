package core.log.domain;

import core.framework.api.json.Property;
import core.framework.log.message.PerformanceStatMessage;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public class ActionLogEntry {
    @Property(name = "id")
    public String id;
    @Property(name = "date")
    public Instant date;
    @Property(name = "app")
    public String app;
    @Property(name = "host")
    public String host;
    @Property(name = "result")
    public String result;
    @Property(name = "action")
    public String action;
    @Property(name = "correlation_ids")
    public List<String> correlationIds;
    @Property(name = "clients")
    public List<String> clients;
    @Property(name = "ref_ids")
    public List<String> refIds;
    @Property(name = "error_code")
    public String errorCode;
    @Property(name = "error_message")
    public String errorMessage;
    @Property(name = "elapsed")
    public Long elapsed;
    @Property(name = "context")
    public Map<String, List<String>> context;
    @Property(name = "stats")
    public Map<String, Double> stats;
    @Property(name = "perf_stats")
    public Map<String, PerformanceStatMessage> performanceStats;
}
