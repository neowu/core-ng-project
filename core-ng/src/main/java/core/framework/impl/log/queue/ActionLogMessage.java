package core.framework.impl.log.queue;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.time.Instant;
import java.util.Map;

/**
 * @author neo
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ActionLogMessage {
    @XmlElement(name = "date")
    public Instant date;
    @XmlElement(name = "app")
    public String app;
    @XmlElement(name = "server_ip")
    public String serverIP;
    @XmlElement(name = "id")
    public String id;
    @XmlElement(name = "result")
    public String result;
    @XmlElement(name = "ref_id")
    public String refId;
    @XmlElement(name = "action")
    public String action;
    @XmlElement(name = "error_code")
    public String errorCode;
    @XmlElement(name = "error_message")
    public String errorMessage;
    @XmlElement(name = "elapsed")
    public Long elapsed;
    @XmlElement(name = "context")
    public Map<String, String> context;
    @XmlElement(name = "perf_stats")
    public Map<String, PerformanceStatMessage> performanceStats;
    @XmlElement(name = "trace_log")
    public String traceLog;
}
