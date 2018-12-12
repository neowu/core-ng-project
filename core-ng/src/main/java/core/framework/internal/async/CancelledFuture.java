package core.framework.internal.async;

import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author neo
 */
class CancelledFuture<T> implements Future<T> {
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return true;
    }

    @Override
    public boolean isCancelled() {
        return true;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public T get() {
        throw new CancellationException();
    }

    @Override
    public T get(long timeout, TimeUnit unit) {
        throw new CancellationException();
    }
}
