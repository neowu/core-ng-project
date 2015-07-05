package core.framework.impl.queue;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author neo
 */
public class MessageHandlerCounter {
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    int maxConcurrentHandlers = 10;
    private final AtomicInteger counter = new AtomicInteger(0);

    public void waitUntilAvailable() throws InterruptedException {
        try {
            lock.lock();
            while (counter.intValue() > maxConcurrentHandlers) {
                condition.await();
            }
        } finally {
            lock.unlock();
        }
    }

    public void increase() {
        try {
            lock.lock();
            counter.getAndIncrement();
        } finally {
            lock.unlock();
        }
    }

    public void decrease() {
        try {
            lock.lock();
            counter.getAndDecrement();
            condition.signal();
        } finally {
            lock.unlock();
        }
    }
}
