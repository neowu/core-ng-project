package core.framework.api.util;

/**
 * @author neo
 */
public final class StopWatch {
    private long start;

    public StopWatch() {
        reset();
    }

    public void reset() {
        start = System.currentTimeMillis();
    }

    public long elapsedTime() {
        long end = System.currentTimeMillis();
        return end - start;
    }
}
