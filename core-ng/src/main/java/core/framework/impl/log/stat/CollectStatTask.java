package core.framework.impl.log.stat;

import core.framework.impl.log.LogForwarder;

import java.util.Map;

/**
 * @author neo
 */
public final class CollectStatTask implements Runnable {
    private final LogForwarder logForwarder;
    private final Stat stat;

    public CollectStatTask(LogForwarder logForwarder, Stat stat) {
        this.logForwarder = logForwarder;
        this.stat = stat;
    }

    @Override
    public void run() {
        Map<String, Double> stats = stat.collect();
        logForwarder.forwardStats(stats);
    }
}
