package core.framework.api.concurrent;

import core.framework.api.log.ActionLogContext;
import core.framework.impl.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * @author neo
 */
public class AsyncExecutor {
    private final Logger logger = LoggerFactory.getLogger(AsyncExecutor.class);
    private final Executor executor;

    public AsyncExecutor(Executor executor) {
        this.executor = executor;
    }

    public <T> Future<T> submit(String name, Callable<T> task) {
        String requestId = ActionLogContext.get(ActionLogContext.REQUEST_ID).orElseGet(() -> UUID.randomUUID().toString());
        Optional<String> actionOptional = ActionLogContext.get(ActionLogContext.ACTION);
        String action = actionOptional.isPresent() ? actionOptional.get() + "/" + name : name;
        boolean trace = "true".equals(ActionLogContext.get(ActionLogContext.TRACE).orElse(null));

        return executor.submit(() -> {
            try {
                logger.debug("=== execution begin ===");
                String executionId = UUID.randomUUID().toString();
                ActionLogContext.put("executionId", executionId);
                ActionLogContext.put(ActionLogContext.REQUEST_ID, requestId);
                ActionLogContext.put(ActionLogContext.ACTION, action);
                if (trace) {
                    logger.warn("trace log is triggered for current execution, executionId={}", executionId);
                    ActionLogContext.put(ActionLogContext.TRACE, Boolean.TRUE);
                }
                return task.call();
            } finally {
                logger.debug("=== execution end ===");
            }
        });
    }
}
