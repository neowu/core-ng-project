package core.framework.search;

/**
 * @author neo
 */
public class UpdateRequest<T> {
    public String index;
    public String id;
    public T source;
    public String script;
}
