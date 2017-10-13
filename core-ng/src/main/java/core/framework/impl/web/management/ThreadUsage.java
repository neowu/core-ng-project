package core.framework.impl.web.management;

import core.framework.api.json.Property;

/**
 * @author neo
 */
public class ThreadUsage {
    @Property(name = "thread_count")
    public Integer threadCount;
    @Property(name = "peak_thread_count")
    public Integer peakThreadCount;
}
