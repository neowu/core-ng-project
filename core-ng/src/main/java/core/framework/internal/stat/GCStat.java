package core.framework.internal.stat;

import java.lang.management.GarbageCollectorMXBean;
import java.util.concurrent.TimeUnit;

/**
 * @author neo
 */
class GCStat {
    final String name;
    private final GarbageCollectorMXBean bean;
    private long previousCount; // generally only one thread collects stat, so no concurrency issue
    private long previousElapsed;

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
