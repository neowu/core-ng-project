package core.framework.impl.mongo;

import org.bson.BsonWriter;

/**
 * @author neo
 */
@FunctionalInterface
public interface EntityEncoder<T> {
    void encode(BsonWriter writer, T entity);
}
