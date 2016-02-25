package core.framework.api.async;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * @author neo
 */
public interface Executor {
    <T> Batch<T> batch(String action);

    <T> Batch<T> batch(String action, int maxConcurrentHandlers);

    <T> Future<T> submit(String action, Callable<T> task);
}
