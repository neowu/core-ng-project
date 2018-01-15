package core.framework.impl.web.http;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author neo
 */
class LRUMap<K, V> extends LinkedHashMap<K, V> {
    private static final long serialVersionUID = -191933814768129858L;

    private final int maxSize;

    LRUMap(int maxSize) {
        super(maxSize, 0.75F, true);
        this.maxSize = maxSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxSize;
    }
}
