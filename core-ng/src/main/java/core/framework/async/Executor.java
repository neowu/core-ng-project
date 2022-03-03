package core.framework.async;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * @author neo
 */
public interface Executor {
    <T> Future<T> submit(String action, Callable<T> task);

    Future<Void> submit(String action, Task task);

    void submit(String action, Task task, Duration delay);
}
