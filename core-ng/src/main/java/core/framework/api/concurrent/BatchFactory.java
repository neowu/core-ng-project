package core.framework.api.concurrent;

import core.framework.impl.concurrent.Executor;
import core.framework.impl.log.LogManager;

/**
 * @author neo
 */
public class BatchFactory {
    private final Executor executor;
    private final LogManager logManager;

    public BatchFactory(Executor executor, LogManager logManager) {
        this.executor = executor;
        this.logManager = logManager;
    }

    public Batch newBatch(String name) {
        return new Batch(name, executor, logManager);
    }
}
