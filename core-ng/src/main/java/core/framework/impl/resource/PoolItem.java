package core.framework.impl.resource;

import java.time.Instant;

/**
 * @author neo
 */
public class PoolItem<T> {
    public final T resource;
    Instant returnTime;
    public boolean broken;

    public PoolItem(T resource) {
        this.resource = resource;
    }
}
