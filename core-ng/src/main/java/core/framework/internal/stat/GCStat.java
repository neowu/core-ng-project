package core.framework.internal.stat;

import javax.annotation.Nullable;
import java.lang.management.GarbageCollectorMXBean;

/**
 * @author neo
 */
final class GCStat {
    @Nullable
    static GCStat of(GarbageCollectorMXBean bean) {
        String name = collector(bean.getName());
        if (name == null) return null;
        return new GCStat(bean, name);
    }

    static String collector(String collectorName) {
        // -XX:+UseG1GC
        if (collectorName.contains("Young")) return "young";
        if (collectorName.contains("Old")) return "old";
        // -XX:+UseParallelGC
        if (collectorName.contains("Scavenge")) return "young";
        if (collectorName.contains("MarkSweep")) return "old";
        return null;   // ignore not supported collector
    }

    final String name;
    private final GarbageCollectorMXBean bean;
    private long previousCount = -1; // generally only one thread collects stat, so no concurrency issue
    private long previousElapsed = -1;

    private GCStat(GarbageCollectorMXBean bean, String name) {
        this.bean = bean;
        this.name = name;
    }

    long count() {
        long previous = previousCount;
        long current = bean.getCollectionCount();
        previousCount = current;
        if (previous == -1) return 0;   // ignore first data point, only count changes, as monitor just started
        return current - previous;
    }

    long elapsed() {
        long previous = previousElapsed;
        long current = bean.getCollectionTime();
        previousElapsed = current;
        if (previous == -1) return 0;   // ignore first data point, only count changes, as monitor just started
        return (current - previous) * 1_000_000; // convert to nano
    }
}
