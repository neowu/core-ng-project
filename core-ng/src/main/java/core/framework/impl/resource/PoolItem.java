package core.framework.impl.resource;

import java.time.Instant;

/**
 * @author neo
 */
public class PoolItem<T extends AutoCloseable> implements AutoCloseable {
    private final Pool<T> pool;
    public final T resource;
    Instant returnTime;
    public boolean broken;

    public PoolItem(Pool<T> pool, T resource) {
        this.pool = pool;
        this.resource = resource;
    }

    @Override
    public void close() {
        pool.put(this);
    }
}
