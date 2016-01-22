package core.framework.api.search;

/**
 * @author neo
 */
public class IndexRequest<T> {
    public String index;
    public String id;
    public T source;
}
