package core.log.domain;

import core.framework.api.json.Property;
import core.framework.search.Index;

import java.time.Instant;

/**
 * @author neo
 */
@Index(index = "trace", type = "trace")
public class TraceDocument {
    @Property(name = "date")
    public Instant date;
    @Property(name = "id")
    public String id;
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
