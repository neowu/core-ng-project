package core.log.domain;

import core.framework.api.search.Index;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.time.Instant;

/**
 * @author neo
 */
@Index(index = "trace", type = "trace")
@XmlAccessorType(XmlAccessType.FIELD)
public class TraceDocument {
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
    @XmlElement(name = "error_code")
    public String errorCode;
    @XmlElement(name = "content")
    public String content;
}
