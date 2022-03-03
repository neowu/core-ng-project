package core.framework.test.async;

import core.framework.async.Executor;
import core.framework.async.Task;
import core.framework.internal.async.CallableTask;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author neo
 */
public class MockExecutor implements Executor {
    @Override
    public <T> Future<T> submit(String action, Callable<T> task) {
        return new ExecutorFuture<>(task);
    }

    @Override
    public Future<Void> submit(String action, Task task) {
        return submit(action, new CallableTask(task));
    }

    @Override
    public void submit(String action, Task task, Duration delay) {
        // ignore all delayed task
    }

    private static class ExecutorFuture<T> implements Future<T> {
        private T result;
        private Throwable error;

        ExecutorFuture(Callable<T> task) {
            try {
                result = task.call();
            } catch (Throwable e) {
                error = e;
            }
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public T get() throws ExecutionException {
            if (error != null) throw new ExecutionException(error);
            return result;
        }

        @Override
        public T get(long timeout, TimeUnit unit) throws ExecutionException {
            return get();
        }
    }
}
