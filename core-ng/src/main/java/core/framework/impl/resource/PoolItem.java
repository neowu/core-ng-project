package core.framework.impl.resource;

/**
 * @author neo
 */
public class PoolItem<T> {
    public final T resource;
    long returnTime;    // according to profiling, use System.currentTimeMillis instead of Instant.now()
    public boolean broken;

    public PoolItem(T resource) {
        this.resource = resource;
    }
}
