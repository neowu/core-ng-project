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
public class StatMessage {
    @XmlElement(name = "id")
    public String id;
    @XmlElement(name = "date")
    public Instant date;
    @XmlElement(name = "app")
    public String app;
    @XmlElement(name = "server_ip")
    public String serverIP;
    @XmlElement(name = "stats")
    public Map<String, Double> stats;
}
