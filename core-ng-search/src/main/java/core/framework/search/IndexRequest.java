package core.framework.search;

import org.jspecify.annotations.Nullable;

/**
 * @author neo
 */
public class IndexRequest<T> {
    @Nullable
    public String index;
    public String id;
    public T source;
}
