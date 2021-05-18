package app.monitor.job;

/**
 * @author neo
 */
public class GCStat {
    final String name;
    private long previousCount = -1;
    private long previousElapsed = -1;

    public GCStat(String name) {
        this.name = name;
    }

    long count(long currentCollectionCount) {
        long previous = previousCount;
        previousCount = currentCollectionCount;
        if (previous == -1) return 0;   // ignore first data point, only count changes, as monitor just started
        long count = currentCollectionCount - previous;
        if (count < 0) return 0; // e.g. target jvm restarted, and monitor is still using old counter
        return count;
    }

    long elapsed(long currentCollectionTime) {
        long previous = previousElapsed;
        previousElapsed = currentCollectionTime;
        if (previous == -1) return 0;   // ignore first data point, only count changes, as monitor just started
        long time = (currentCollectionTime - previous) * 1_000_000;
        if (time < 0) return 0; // e.g. target jvm restarted, and monitor is still using old counter
        return time; // convert to nano
    }
}
