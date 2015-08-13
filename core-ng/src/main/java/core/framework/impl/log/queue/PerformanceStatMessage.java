package core.framework.impl.log.queue;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author neo
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class PerformanceStatMessage {
    @XmlElement(name = "total_elapsed")
    public Long totalElapsed;
    @XmlElement(name = "count")
    public Integer count;
}
