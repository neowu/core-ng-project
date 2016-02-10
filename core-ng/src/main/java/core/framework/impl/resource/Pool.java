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
 * is to keep original exception, and simplify context variable access (read or write var within method),
 * <p>
 * the downside is boilerplate code, so to keep it only for internal
 *
 * @author neo
 */
public final class Pool<T> {
    final BlockingDeque<PoolItem<T>> idleItems = new LinkedBlockingDeque<>();
    final AtomicInteger total = new AtomicInteger(0);
    private final Logger logger = LoggerFactory.getLogger(Pool.class);
    private final Supplier<T> factory;
    private final ResourceCloseHandler<T> closeHandler;
    String name;
    int minSize = 1;
    int maxSize = 50;
    private Duration maxIdleTime = Duration.ofMinutes(30);
    private long checkoutTimeoutInMs = Duration.ofSeconds(30).toMillis();

    public Pool(Supplier<T> factory, ResourceCloseHandler<T> closeHandler) {
        this.factory = factory;
        this.closeHandler = closeHandler;
    }

    public void name(String name) {
        this.name = name;
    }

    public void size(int minSize, int maxSize) {
        this.minSize = minSize;
        this.maxSize = maxSize;
    }

    public void maxIdleTime(Duration maxIdleTime) {
        this.maxIdleTime = maxIdleTime;
    }

    public void checkoutTimeout(Duration checkoutTimeout) {
        checkoutTimeoutInMs = checkoutTimeout.toMillis();
    }

    public PoolItem<T> borrowItem() {
        PoolItem<T> item = idleItems.poll();
        if (item != null) return item;

        if (total.get() < maxSize) {
            return createNewItem();
        } else {
            return waitNextAvailableItem();
        }
    }

    public void returnItem(PoolItem<T> item) {
        if (item.broken) {
            recycleItem(item);
        } else {
            item.returnTime = System.currentTimeMillis();
            idleItems.push(item);
        }
    }

    private void recycleItem(PoolItem<T> item) {
        StopWatch watch = new StopWatch();
        int total = this.total.decrementAndGet();
        try {
            closeResource(item.resource);
        } finally {
            logger.debug("recycle resource, pool={}, total={}, elapsed={}", name, total, watch.elapsedTime());
        }
    }

    private PoolItem<T> waitNextAvailableItem() {
        StopWatch watch = new StopWatch();
        try {
            PoolItem<T> item = idleItems.poll(checkoutTimeoutInMs, TimeUnit.MILLISECONDS);
            if (item == null) throw new PoolException("timeout to wait for next available resource", "POOL_TIME_OUT");
            return item;
        } catch (InterruptedException e) {
            throw new Error("interrupted during waiting for next available resource", e);
        } finally {
            logger.debug("wait for next available resource, pool={}, total={}, elapsed={}", name, total.get(), watch.elapsedTime());
        }
    }

    private PoolItem<T> createNewItem() {
        StopWatch watch = new StopWatch();
        total.incrementAndGet();
        try {
            return new PoolItem<>(factory.get());
        } catch (Throwable e) {
            total.getAndDecrement();
            throw e;
        } finally {
            logger.debug("create new resource, pool={}, total={}, elapsed={}", name, total.get(), watch.elapsedTime());
        }
    }

    public void refresh() {
        logger.info("refresh resource pool, pool={}", name);
        recycleIdleItems();
        replenish();
    }

    private void recycleIdleItems() {
        Iterator<PoolItem<T>> iterator = idleItems.descendingIterator();
        long maxIdleTimeInSeconds = maxIdleTime.getSeconds();
        Instant now = Instant.now();

        while (iterator.hasNext()) {
            PoolItem<T> item = iterator.next();
            if (Duration.between(Instant.ofEpochMilli(item.returnTime), now).getSeconds() > maxIdleTimeInSeconds) {
                boolean removed = idleItems.remove(item);
                if (!removed) return;
                recycleItem(item);
            } else {
                return;
            }
        }
    }

    private void replenish() {
        while (total.get() < minSize) {
            returnItem(createNewItem());
        }
    }

    private void closeResource(T resource) {
        try {
            closeHandler.close(resource);
        } catch (Exception e) {
            logger.warn("failed to close resource, pool={}", name, e);
        }
    }

    public void close() {
        total.set(maxSize);   // make sure no more new resource will be created
        while (true) {
            PoolItem<T> item = idleItems.poll();
            if (item == null) return;
            closeResource(item.resource);
        }
    }
}
