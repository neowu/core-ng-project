package core.framework.impl.log.stat;

import core.framework.impl.log.LogForwarder;
import core.framework.util.ASCII;
import core.framework.util.Lists;
import core.framework.util.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author neo
 */
public class CollectStatsTask implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(CollectStatsTask.class);
    private final OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
    private final ThreadMXBean thread = ManagementFactory.getThreadMXBean();
    private final MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
    private final List<GCStat> gcStats;
    private final LogForwarder logForwarder;
    private final List<Metrics> metrics;

    public CollectStatsTask(LogForwarder logForwarder, List<Metrics> metrics) {
        this.logForwarder = logForwarder;
        this.metrics = metrics;

        gcStats = Lists.newArrayList();
        List<GarbageCollectorMXBean> beans = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean bean : beans) {
            String name = garbageCollectorName(bean.getName());
            gcStats.add(new GCStat(name, bean));
        }
    }

    @Override
    public void run() {
        Map<String, Double> stats = Maps.newLinkedHashMap();
        collect(stats);
        logForwarder.forwardStats(stats);
    }

    void collect(Map<String, Double> stats) {
        stats.put("sys_load_avg", os.getSystemLoadAverage());
        stats.put("thread_count", (double) thread.getThreadCount());
        MemoryUsage usage = memory.getHeapMemoryUsage();
        stats.put("jvm_heap_used", (double) usage.getUsed());
        stats.put("jvm_heap_max", (double) usage.getMax());

        for (GCStat gcStat : gcStats) {
            long count = gcStat.count();
            long elapsedTime = gcStat.elapsedTime();
            stats.put("jvm_gc_" + gcStat.name + "_count", (double) count);
            stats.put("jvm_gc_" + gcStat.name + "_total_elapsed", (double) elapsedTime);
        }

        for (Metrics metrics : metrics) {
            try {
                metrics.collect(stats);
            } catch (Throwable e) {
                logger.warn("failed to collect metrics, metrics={}, error={}", metrics.getClass().getCanonicalName(), e.getMessage(), e);
            }
        }
    }

    String garbageCollectorName(String name) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (ch == ' ') builder.append('_');
            else builder.append(ASCII.toLowerCase(ch));
        }
        return builder.toString();
    }

    static class GCStat {
        final String name;
        final GarbageCollectorMXBean bean;
        long previousCount;
        long previousElapsedTime;

        GCStat(String name, GarbageCollectorMXBean bean) {
            this.name = name;
            this.bean = bean;
        }

        long count() {
            long previous = previousCount;
            long current = bean.getCollectionCount();
            previousCount = current;
            return current - previous;
        }

        long elapsedTime() {
            long previous = previousElapsedTime;
            long current = bean.getCollectionTime();
            previousElapsedTime = current;
            return TimeUnit.NANOSECONDS.convert(current - previous, TimeUnit.MILLISECONDS);
        }
    }
}
