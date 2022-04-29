package core.framework.internal.util;

import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author neo
 */
public class LRUMap<K, V> extends LinkedHashMap<K, V> { // not thread safe
    @Serial
    private static final long serialVersionUID = -191933814768129858L;

    private final int maxSize;

    public LRUMap(int maxSize) {
        super(maxSize, 0.75F, true);
        this.maxSize = maxSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxSize;
    }
}
