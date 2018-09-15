package core.framework.impl.log.stat;

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
public class Stat {
    public final List<Metrics> metrics = Lists.newArrayList();

    private final Logger logger = LoggerFactory.getLogger(Stat.class);
    private final OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
    private final ThreadMXBean thread = ManagementFactory.getThreadMXBean();
    private final MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
    private final List<GCStat> gcStats = Lists.newArrayList();

    public Stat() {
        List<GarbageCollectorMXBean> beans = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean bean : beans) {
            String name = garbageCollectorName(bean.getName());
            gcStats.add(new GCStat(name, bean));
        }
    }

    public Map<String, Double> collect() {
        Map<String, Double> stats = Maps.newLinkedHashMap();

        stats.put("sys_load_avg", os.getSystemLoadAverage());
        stats.put("thread_count", (double) thread.getThreadCount());
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

    private void collectMetrics(Map<String, Double> stats) {
        for (Metrics metrics : metrics) {
            try {
                metrics.collect(stats);
            } catch (Throwable e) {
                logger.warn("failed to collect metrics, metrics={}, error={}", metrics.getClass().getCanonicalName(), e.getMessage(), e);
            }
        }
    }

    final String garbageCollectorName(String name) {
        var builder = new StringBuilder();
        int length = name.length();
        for (int i = 0; i < length; i++) {
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
        long previousElapsed;

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

        long elapsed() {
            long previous = previousElapsed;
            long current = bean.getCollectionTime();
            previousElapsed = current;
            return TimeUnit.NANOSECONDS.convert(current - previous, TimeUnit.MILLISECONDS);
        }
    }
}
