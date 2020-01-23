package core.framework.internal.stat;

import core.framework.util.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.util.List;

/**
 * @author neo
 */
public class StatCollector {
    public final List<Metrics> metrics = Lists.newArrayList();

    private final Logger logger = LoggerFactory.getLogger(StatCollector.class);
    private final OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
    private final ThreadMXBean thread = ManagementFactory.getThreadMXBean();
    private final CPUStat cpuStat = new CPUStat(thread, os.getAvailableProcessors());
    private final MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
    private final List<GCStat> gcStats = Lists.newArrayList();

    public StatCollector() {
        List<GarbageCollectorMXBean> beans = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean bean : beans) {
            gcStats.add(new GCStat(bean));
        }
    }

    public Stats collect() {
        Stats stats = new Stats();

        stats.put("sys_load_avg", os.getSystemLoadAverage());
        stats.put("cpu_usage", cpuStat.usage());
        stats.put("thread_count", thread.getThreadCount());
        MemoryUsage usage = memory.getHeapMemoryUsage();
        stats.put("jvm_heap_used", (double) usage.getUsed());
        stats.put("jvm_heap_max", (double) usage.getMax());

        for (GCStat gcStat : gcStats) {
            long count = gcStat.count();
            long elapsed = gcStat.elapsed();
            stats.put("jvm_gc_" + gcStat.name + "_count", (double) count);
            stats.put("jvm_gc_" + gcStat.name + "_total_elapsed", (double) elapsed);
        }

        collectMetrics(stats);
        return stats;
    }

    private void collectMetrics(Stats stats) {
        for (Metrics metrics : metrics) {
            try {
                metrics.collect(stats);
            } catch (Throwable e) {
                logger.warn("failed to collect metrics, metrics={}, error={}", metrics.getClass().getCanonicalName(), e.getMessage(), e);
            }
        }
    }
}
