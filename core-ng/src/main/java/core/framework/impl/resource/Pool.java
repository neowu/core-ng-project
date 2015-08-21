package core.framework.impl.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * this is for internal use only
 *
 * @author neo
 */
public class Pool<T extends AutoCloseable> {
    private final Logger logger = LoggerFactory.getLogger(Pool.class);

    private final int minSize;
    private final int maxSize;
    private final Duration maxIdleTime;
    private final Supplier<T> supplier;

    private final AtomicInteger currentSize = new AtomicInteger(0);
    final BlockingDeque<PoolItem<T>> queue = new LinkedBlockingDeque<>();

    public Pool(Supplier<T> supplier, int minSize, int maxSize, Duration maxIdleTime) {
        this.supplier = supplier;
        this.minSize = minSize;
        this.maxSize = maxSize;
        this.maxIdleTime = maxIdleTime;
    }

    public PoolItem<T> take() {
        PoolItem<T> item = queue.poll();
        if (item != null) return item;

        if (currentSize.get() < maxSize) {
            int currentSize = this.currentSize.getAndIncrement();
            try {
                logger.debug("create new resource, currentSize={}", currentSize);
                return new PoolItem<>(this, supplier.get());
            } catch (Throwable e) {
                this.currentSize.getAndDecrement();
                throw e;
            }
        } else {
            try {
                return queue.poll(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new Error("failed to retrieve resource from pool", e);
            }
        }
    }

    public void put(PoolItem<T> item) {
        if (item.broken) {
            currentSize.getAndDecrement();
            closeResource(item);
        } else {
            item.returnTime = Instant.now();
            queue.push(item);
        }
    }

    public void initialize() {
        while (currentSize.get() < minSize) {
            put(new PoolItem<>(this, supplier.get()));
            currentSize.getAndIncrement();
        }
    }

    public void close() {
        currentSize.set(maxSize);   // make sure no more new resource will be created
        while (true) {
            PoolItem<T> item = queue.poll();
            if (item == null) return;
            closeResource(item);
        }
    }

    public void clearIdleItems() {
        Iterator<PoolItem<T>> iterator = queue.descendingIterator();
        long maxIdleTimeInSeconds = maxIdleTime.getSeconds();
        Instant now = Instant.now();

        while (iterator.hasNext()) {
            PoolItem<T> item = iterator.next();
            if (Duration.between(item.returnTime, now).getSeconds() > maxIdleTimeInSeconds && currentSize.get() > minSize) {
                boolean removed = queue.remove(item);
                if (removed) {
                    currentSize.getAndDecrement();
                    closeResource(item);
                }
            } else {
                break;
            }
        }
    }

    private void closeResource(PoolItem<T> item) {
        try {
            item.resource.close();
        } catch (Exception e) {
            logger.warn("failed to close resource", e);
        }
    }
}
