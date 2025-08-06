package core.framework.mongo.impl;

import org.jspecify.annotations.Nullable;

/**
 * @author neo
 */
public interface EntityIdHandler<T> {
    @Nullable
    Object get(T entity);

    void set(T entity, Object id);

    boolean generateIdIfAbsent();
}
