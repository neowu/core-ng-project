package core.framework.impl.resource;

import core.framework.api.util.StopWatch;
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
 * this is for internal use only,
 * <p>
 * the reason not using template/lambda pattern but classic design
 * is to keep original exception, and simplify value writing,
 * <p>
 * the downside is boilerplate code, so to keep it only for internal
 *
 * @author neo
 */
public class Pool<T> {
    private final Logger logger = LoggerFactory.getLogger(Pool.class);

    private final Supplier<T> factory;
    private final ResourceCloseHandler<T> closeHandler;

    final BlockingDeque<PoolItem<T>> queue = new LinkedBlockingDeque<>();
    final AtomicInteger currentSize = new AtomicInteger(0);

    String name;
    int minSize = 1;
    int maxSize = 5;
    private Duration maxIdleTime = Duration.ofMinutes(30);

    public Pool(Supplier<T> factory, ResourceCloseHandler<T> closeHandler) {
        this.factory = factory;
        this.closeHandler = closeHandler;
    }

    public void name(String name) {
        this.name = name;
    }

    public void configure(int minSize, int maxSize, Duration maxIdleTime) {
        this.minSize = minSize;
        this.maxSize = maxSize;
        this.maxIdleTime = maxIdleTime;
    }

    public PoolItem<T> borrowItem() {
        PoolItem<T> item = queue.poll();
        if (item != null) return item;

        if (currentSize.get() < maxSize) {
            return createNewItem();
        } else {
            return waitNextAvailableItem();
        }
    }

    public void returnItem(PoolItem<T> item) {
        if (item.broken) {
            recycleItem(item);
        } else {
            item.returnTime = Instant.now();
            queue.push(item);
        }
    }

    private void recycleItem(PoolItem<T> item) {
        StopWatch watch = new StopWatch();
        int previousSize = currentSize.getAndDecrement();
        try {
            closeResource(item.resource);
        } finally {
            logger.debug("[pool:{}] recycle resource, previousSize={}, elapsed={}", name, previousSize, watch.elapsedTime());
        }
    }

    private PoolItem<T> waitNextAvailableItem() {
        StopWatch watch = new StopWatch();
        try {
            PoolItem<T> item = queue.poll(30, TimeUnit.SECONDS);
            if (item == null) throw new Error("timeout to wait for next available resource");
            return item;
        } catch (InterruptedException e) {
            throw new Error("interrupted during waiting for next available resource", e);
        } finally {
            logger.debug("[pool:{}] wait for next available resource, currentSize={}, elapsed={}", name, currentSize.get(), watch.elapsedTime());
        }
    }

    private PoolItem<T> createNewItem() {
        StopWatch watch = new StopWatch();
        int previousSize = currentSize.getAndIncrement();
        try {
            return new PoolItem<>(factory.get());
        } catch (Throwable e) {
            currentSize.getAndDecrement();
            throw e;
        } finally {
            logger.debug("[pool:{}] create new resource, previousSize={}, elapsed={}", name, previousSize, watch.elapsedTime());
        }
    }

    void replenish() {
        while (currentSize.get() < minSize) {
            returnItem(createNewItem());
        }
    }

    void recycleIdleItems() {
        Iterator<PoolItem<T>> iterator = queue.descendingIterator();
        long maxIdleTimeInSeconds = maxIdleTime.getSeconds();
        Instant now = Instant.now();

        while (iterator.hasNext()) {
            PoolItem<T> item = iterator.next();
            if (Duration.between(item.returnTime, now).getSeconds() > maxIdleTimeInSeconds) {
                boolean removed = queue.remove(item);
                if (!removed) return;
                recycleItem(item);
            } else {
                return;
            }
        }
    }

    private void closeResource(T resource) {
        try {
            closeHandler.close(resource);
        } catch (Exception e) {
            logger.warn("[pool:{}] failed to close resource", name, e);
        }
    }

    public void close() {
        currentSize.set(maxSize);   // make sure no more new resource will be created
        while (true) {
            PoolItem<T> item = queue.poll();
            if (item == null) return;
            closeResource(item.resource);
        }
    }
}
