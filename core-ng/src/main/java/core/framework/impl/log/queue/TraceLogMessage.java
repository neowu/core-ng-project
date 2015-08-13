package core.framework.impl.log.queue;

import core.framework.api.queue.Message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.time.Instant;
import java.util.List;

/**
 * @author neo
 */
@Message(name = "trace_log")
@XmlAccessorType(XmlAccessType.FIELD)
public class TraceLogMessage {
    @XmlElement(name = "date")
    public Instant date;
    @XmlElement(name = "id")
    public String id;
    @XmlElement(name = "app")
    public String app;
    @XmlElement(name = "result")
    public String result;
    @XmlElement(name = "action")
    public String action;
    @XmlElement(name = "content")
    public List<String> content;
}
