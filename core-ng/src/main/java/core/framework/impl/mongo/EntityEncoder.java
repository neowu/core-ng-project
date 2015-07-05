package core.framework.impl.mongo;

import org.bson.BsonWriter;

/**
 * @author neo
 */
public interface EntityEncoder<T> {
    void encode(BsonWriter writer, T entity);
}
