package core.framework.impl.mongo;

/**
 * @author neo
 */
public interface EntityIdHandler<T> {
    Object get(T entity);

    void set(T entity, Object id);

    boolean generateIdIfAbsent();
}
