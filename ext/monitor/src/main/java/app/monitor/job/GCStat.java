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
        return currentCollectionCount - previous;
    }

    long elapsed(long currentCollectionTime) {
        long previous = previousElapsed;
        previousElapsed = currentCollectionTime;
        if (previous == -1) return 0;   // ignore first data point, only count changes, as monitor just started
        return (currentCollectionTime - previous) * 1_000_000; // convert to nano
    }
}
