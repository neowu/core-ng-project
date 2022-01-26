package core.framework.search;

import javax.annotation.Nullable;

/**
 * @author miller
 */
public class IndexWithRoutingRequest<T> {
    @Nullable
    public String index;
    public String id;
    public String routing;
    public T source;
}
