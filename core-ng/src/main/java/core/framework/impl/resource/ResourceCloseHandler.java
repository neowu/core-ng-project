package core.framework.impl.resource;

/**
 * @author neo
 */
public interface ResourceCloseHandler<T> {
    void close(T resource) throws Exception;
}
