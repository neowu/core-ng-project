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
import java.text.NumberFormat;
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

    public double highCPUUsageThreshold = 0.8;
    public double highHeapUsageThreshold = 0.8;

    public StatCollector() {
        List<GarbageCollectorMXBean> beans = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean bean : beans) {
            gcStats.add(new GCStat(bean));
        }
    }

    public Stats collect() {
        Stats stats = new Stats();

        collectCPUUsage(stats);
        stats.put("thread_count", thread.getThreadCount());
        collectHeapUsage(stats);

        for (GCStat gcStat : gcStats) {
            long count = gcStat.count();
            long elapsed = gcStat.elapsed();
            stats.put("jvm_gc_" + gcStat.name + "_count", (double) count);
            stats.put("jvm_gc_" + gcStat.name + "_total_elapsed", (double) elapsed);
        }

        collectMetrics(stats);
        return stats;
    }

    private void collectHeapUsage(Stats stats) {
        MemoryUsage usage = memory.getHeapMemoryUsage();
        double usedHeap = usage.getUsed();
        double maxHeap = usage.getMax();
        stats.put("jvm_heap_used", usedHeap);
        stats.put("jvm_heap_max", maxHeap);
        checkHighHeapUsage(usedHeap, maxHeap, stats);
    }

    private void collectCPUUsage(Stats stats) {
        stats.put("sys_load_avg", os.getSystemLoadAverage());

        double cpuUsage = cpuStat.usage();
        stats.put("cpu_usage", cpuUsage);
        checkHighCPUUsage(cpuUsage, stats);
    }

    void checkHighCPUUsage(double usage, Stats stats) {
        if (usage >= highCPUUsageThreshold) {
            NumberFormat format = NumberFormat.getPercentInstance();
            stats.warn("HIGH_CPU_USAGE", "cpu usage is too high, usage=" + format.format(usage));
        }
    }

    void checkHighHeapUsage(double usedHeap, double maxHeap, Stats stats) {
        double usage = usedHeap / maxHeap;
        if (usage >= highHeapUsageThreshold) {
            NumberFormat format = NumberFormat.getPercentInstance();
            stats.warn("HIGH_HEAP_USAGE", "cpu usage is too high, usage=" + format.format(usage));
        }
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
