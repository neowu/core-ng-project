package core.framework.impl.resource;

/**
 * @author neo
 */
@FunctionalInterface
public interface ResourceCloseHandler<T> {
    void close(T resource) throws Exception;
}
