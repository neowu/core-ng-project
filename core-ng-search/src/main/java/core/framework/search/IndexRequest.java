package core.framework.search;

import javax.annotation.Nullable;

/**
 * @author neo
 */
public class IndexRequest<T> {
    @Nullable
    public String index;
    public String id;
    public T source;
}
