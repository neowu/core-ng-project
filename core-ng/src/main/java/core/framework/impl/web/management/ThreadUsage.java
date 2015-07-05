package core.framework.impl.web.management;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author neo
 */
@XmlRootElement(name = "thread_usage")
@XmlAccessorType(XmlAccessType.FIELD)
public class ThreadUsage {
    @XmlElement(name = "thread_count")
    public Integer threadCount;
    @XmlElement(name = "peak_thread_count")
    public Integer peakThreadCount;
}
