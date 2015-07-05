package core.framework.impl.mongo;

import org.bson.types.ObjectId;

/**
 * @author neo
 */
public interface EntityIdHandler<T> {
    ObjectId get(T entity);

    void set(T entity, ObjectId id);
}
