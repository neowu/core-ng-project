package core.framework.impl.log.stat;

import core.framework.api.util.Maps;
import core.framework.impl.log.LogForwarder;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.util.Map;

/**
 * @author neo
 */
public class CollectStatTask implements Runnable {
    private final OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
    private final ThreadMXBean thread = ManagementFactory.getThreadMXBean();
    private final MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
    private final LogForwarder logForwarder;

    public CollectStatTask(LogForwarder logForwarder) {
        this.logForwarder = logForwarder;
    }

    @Override
    public void run() {
        Map<String, Double> stats = Maps.newLinkedHashMap();
        stats.put("sys_load_avg", os.getSystemLoadAverage());
        stats.put("thread_count", (double) thread.getThreadCount());
        MemoryUsage usage = memory.getHeapMemoryUsage();
        stats.put("jvm_heap_used", (double) usage.getUsed());
        stats.put("jvm_heap_max", (double) usage.getMax());
        logForwarder.forwardStats(stats);
    }
}
