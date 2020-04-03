package core.framework.log.message;

import core.framework.api.json.Property;

/**
 * @author neo
 */
public class PerformanceStat {
    @Property(name = "total_elapsed")
    public Long totalElapsed = 0L;
    @Property(name = "count")
    public Integer count = 0;
    @Property(name = "read_entries")
    public Integer readEntries;
    @Property(name = "write_entries")
    public Integer writeEntries;
}
