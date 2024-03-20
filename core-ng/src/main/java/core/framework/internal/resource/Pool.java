package core.framework.internal.resource;

import core.framework.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Iterator;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static core.framework.log.Markers.errorCode;

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
public class Pool<T extends AutoCloseable> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Pool.class);

    // helper for closing resource on creation failure
    public static void closeQuietly(AutoCloseable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (Exception e) {
                LOGGER.warn("failed to close resource", e);
            }
        }
    }

    final BlockingDeque<PoolItem<T>> idleItems = new LinkedBlockingDeque<>();
    final String name;
    final AtomicInteger size = new AtomicInteger(0);
    private final Supplier<T> factory;
    public Duration maxIdleTime = Duration.ofMinutes(30);
    private int minSize = 1;
    private int maxSize = 50;
    private long checkoutTimeoutInMs = Duration.ofSeconds(30).toMillis();
    private ResourceValidator<T> validator;
    private long aliveWindowInMs;    // not to validate if last return time within the window

    public Pool(Supplier<T> factory, String name) {
        this.factory = factory;
        this.name = name;
    }

    public void size(int minSize, int maxSize) {
        this.minSize = minSize;
        this.maxSize = maxSize;
    }

    public void checkoutTimeout(Duration timeout) {
        checkoutTimeoutInMs = timeout.toMillis();
    }

    public void validator(ResourceValidator<T> validator, Duration aliveWindow) {
        this.validator = validator;
        aliveWindowInMs = aliveWindow.toMillis();
    }

    public PoolItem<T> borrowItem() {
        while (true) {
            PoolItem<T> item = idleItems.poll();
            if (item != null) {
                if (validate(item)) return item;
                else continue;
            }

            if (size.get() < maxSize) {
                return createNewItem();         // do not need to check newly created resource
            } else {
                return waitNextAvailableItem(); // do not need to check valid since it's just returned resource
            }
        }
    }

    private boolean validate(PoolItem<T> item) {
        if (validator == null || System.currentTimeMillis() - item.returnTime < aliveWindowInMs) return true;
        boolean valid;
        try {
            valid = validator.validate(item.resource);
        } catch (Throwable e) {     // catch all exceptions to make sure item will be closed to avoid resource leak
            LOGGER.warn(e.getMessage(), e);
            valid = false;
        }
        if (!valid) {
            LOGGER.warn(errorCode("BROKEN_POOL_CONNECTION"), "connection is broken, try to reconnect immediately, pool={}", name);
            closeItem(item);
        }
        return valid;
    }

    public void returnItem(PoolItem<T> item) {
        if (item.broken) {
            // not to replenish new item if current is broken to keep it simple,
            // if pool is full and someone is waiting for resource, there will be other to release resource soon,
            // as the broken resource is rare case
            closeItem(item);
        } else {
            item.returnTime = System.currentTimeMillis();
            idleItems.push(item);
        }
    }

    private PoolItem<T> waitNextAvailableItem() {
        var watch = new StopWatch();
        try {
            PoolItem<T> item = idleItems.poll(checkoutTimeoutInMs, TimeUnit.MILLISECONDS);
            if (item == null) throw new PoolException("timeout to wait for next available resource", "POOL_TIME_OUT");
            return item;
        } catch (InterruptedException e) {
            throw new Error("interrupted during waiting for next available resource", e);
        } finally {
            LOGGER.debug("wait for next available resource, pool={}, elapsed={}", name, watch.elapsed());
        }
    }

    private PoolItem<T> createNewItem() {
        var watch = new StopWatch();
        size.incrementAndGet();
        PoolItem<T> item = null;
        try {
            item = new PoolItem<>(factory.get());
            return item;
        } catch (Throwable e) {
            size.getAndDecrement();
            throw e;
        } finally {
            LOGGER.debug("create new resource, pool={}, item={}, elapsed={}", name, item == null ? null : item.resource, watch.elapsed());
        }
    }

    public void refresh() {
        LOGGER.info("refresh resource pool, pool={}", name);
        evictIdleItems();
        replenish();
    }

    int activeCount() {
        return totalCount() - idleItems.size();
    }

    int totalCount() {
        return size.get();
    }

    private void evictIdleItems() {
        Iterator<PoolItem<T>> iterator = idleItems.descendingIterator();
        long maxIdleTimeInMs = maxIdleTime.toMillis();
        long now = System.currentTimeMillis();

        while (iterator.hasNext()) {
            PoolItem<T> item = iterator.next();
            if (now - item.returnTime >= maxIdleTimeInMs) {
                boolean removed = idleItems.remove(item);
                if (!removed) return;
                closeItem(item);
            } else {
                return;
            }
        }
    }

    private void replenish() {
        while (size.get() < minSize) {
            returnItem(createNewItem());
        }
    }

    private void closeItem(PoolItem<T> item) {
        size.decrementAndGet();
        closeResource(item);
    }

    private void closeResource(PoolItem<T> item) {
        LOGGER.debug("close resource, pool={}, item={}", name, item.resource);
        try {
            item.resource.close();
        } catch (Exception e) {
            LOGGER.warn("failed to close resource, pool={}", name, e);
        }
    }

    public void close() {
        size.set(maxSize);   // make sure no more new resource will be created
        while (true) {
            PoolItem<T> item = idleItems.poll();
            if (item == null) return;
            closeResource(item);
        }
    }
}
