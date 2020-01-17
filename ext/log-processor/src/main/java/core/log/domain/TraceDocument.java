package core.log.domain;

import core.framework.api.json.Property;
import core.framework.search.Index;

import java.time.Instant;

/**
 * @author neo
 */
@Index(name = "trace")
public class TraceDocument {
    @Property(name = "@timestamp")
    public Instant timestamp;
    @Property(name = "app")
    public String app;
    @Property(name = "result")
    public String result;
    @Property(name = "action")
    public String action;
    @Property(name = "error_code")
    public String errorCode;
    @Property(name = "content")
    public String content;
}
