package core.framework.internal.stat;

import core.framework.util.ASCII;

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

    GCStat(GarbageCollectorMXBean bean) {
        this.name = garbageCollectorName(bean.getName());
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
}
