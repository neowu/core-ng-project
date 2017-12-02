package core.framework.test.async;

import core.framework.async.Executor;

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
