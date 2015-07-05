package core.framework.impl.web.management;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author neo
 */
@XmlRootElement(name = "memory_usage")
@XmlAccessorType(XmlAccessType.FIELD)
public class MemoryUsage {
    @XmlElement(name = "heap_init")
    public Long heapInit;
    @XmlElement(name = "heap_used")
    public Long heapUsed;
    @XmlElement(name = "heap_committed")
    public Long heapCommitted;
    @XmlElement(name = "heap_max")
    public Long heapMax;

    @XmlElement(name = "non_heap_init")
    public Long nonHeapInit;
    @XmlElement(name = "non_heap_used")
    public Long nonHeapUsed;
    @XmlElement(name = "non_heap_committed")
    public Long nonHeapCommitted;
    @XmlElement(name = "non_heap_max")
    public Long nonHeapMax;
}
