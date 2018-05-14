package core.framework.mongo.impl;

import org.bson.BsonWriter;

/**
 * @author neo
 */
@FunctionalInterface
public interface EntityEncoder<T> {
    void encode(BsonWriter writer, T entity);
}
