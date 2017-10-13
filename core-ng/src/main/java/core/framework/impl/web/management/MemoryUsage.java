package core.framework.impl.web.management;

import core.framework.api.json.Property;

/**
 * @author neo
 */
public class MemoryUsage {
    @Property(name = "heap_init")
    public Long heapInit;
    @Property(name = "heap_used")
    public Long heapUsed;
    @Property(name = "heap_committed")
    public Long heapCommitted;
    @Property(name = "heap_max")
    public Long heapMax;

    @Property(name = "non_heap_init")
    public Long nonHeapInit;
    @Property(name = "non_heap_used")
    public Long nonHeapUsed;
    @Property(name = "non_heap_committed")
    public Long nonHeapCommitted;
    @Property(name = "non_heap_max")
    public Long nonHeapMax;
}
