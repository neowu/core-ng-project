package core.framework.internal.stat;

import com.sun.management.OperatingSystemMXBean;
import core.framework.util.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;

/**
 * @author neo
 */
public class StatCollector {
    public final List<Metrics> metrics = Lists.newArrayList();

    private final Logger logger = LoggerFactory.getLogger(StatCollector.class);
    private final OperatingSystemMXBean os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    private final ThreadMXBean thread = ManagementFactory.getThreadMXBean();
    private final MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
    private final List<GCStat> gcStats = new ArrayList<>(2);
    private final CPUStat cpuStat = new CPUStat(os);

    public double highCPUUsageThreshold = 0.8;
    public double highHeapUsageThreshold = 0.8;

    public StatCollector() {
        List<GarbageCollectorMXBean> beans = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean bean : beans) {
            GCStat stat = GCStat.of(bean);
            if (stat != null) gcStats.add(stat);
        }
    }

    public Stats collect() {
        var stats = new Stats();

        collectCPUUsage(stats);
        stats.put("thread_count", thread.getThreadCount());
        collectMemoryUsage(stats);

        for (GCStat gcStat : gcStats) {
            long count = gcStat.count();
            long elapsed = gcStat.elapsed();
            stats.put("jvm_gc_" + gcStat.name + "_count", (double) count);
            stats.put("jvm_gc_" + gcStat.name + "_elapsed", (double) elapsed);
        }

        collectMetrics(stats);
        return stats;
    }

    private void collectMemoryUsage(Stats stats) {
        MemoryUsage heapUsage = memory.getHeapMemoryUsage();
        double usedHeap = heapUsage.getUsed();
        double maxHeap = heapUsage.getMax();
        stats.put("jvm_heap_used", usedHeap);
        stats.put("jvm_heap_max", maxHeap);
        stats.checkHighUsage(usedHeap / maxHeap, highHeapUsageThreshold, "heap");

        MemoryUsage nonHeapUsage = memory.getNonHeapMemoryUsage();
        stats.put("jvm_non_heap_used", nonHeapUsage.getUsed());
    }

    private void collectCPUUsage(Stats stats) {
        stats.put("sys_load_avg", os.getSystemLoadAverage());   // until java 14, OperatingSystemMXBean returns host level load and cpu usage, not container level

        double usage = cpuStat.usage();
        stats.put("cpu_usage", usage);
        boolean highUsage = stats.checkHighUsage(usage, highCPUUsageThreshold, "cpu");
        if (highUsage) {
            stats.info("thread_dump", Diagnostic.thread());
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
