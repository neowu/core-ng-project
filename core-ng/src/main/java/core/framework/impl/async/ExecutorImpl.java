package core.framework.impl.async;

import core.framework.api.async.Batch;
import core.framework.api.async.Executor;
import core.framework.impl.log.ActionLog;
import core.framework.impl.log.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author neo
 */
public final class ExecutorImpl implements Executor {
    private final Logger logger = LoggerFactory.getLogger(ExecutorImpl.class);
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final LogManager logManager;

    public ExecutorImpl(LogManager logManager) {
        this.logManager = logManager;
    }

    public void stop() {
        logger.info("stop executor");
        executorService.shutdown();
    }

    @Override
    public <T> Batch<T> batch(String action) {
        return new Batch<>(action, this);
    }

    @Override
    public <T> Future<T> submit(String action, Callable<T> task) {
        // do not log intentionally, so in batch pattern where one batch submit task for each item, the batch process won't reach max trace log line limit.
        ActionLog parentActionLog = logManager.currentActionLog();
        String childAction = parentActionLog != null ? parentActionLog.action + "/" + action : action;
        String refId = parentActionLog != null ? parentActionLog.refId() : null;
        boolean trace = parentActionLog != null && parentActionLog.trace;
        return executorService.submit(new Task<>(task, logManager, childAction, refId, trace));
    }
}
