package core.framework.internal.resource;

/**
 * @author neo
 */
public final class PoolItem<T> {
    public final T resource;
    public boolean broken;
    long returnTime;    // according to profiling, use System.currentTimeMillis instead of Instant.now()

    public PoolItem(T resource) {
        this.resource = resource;
    }
}
