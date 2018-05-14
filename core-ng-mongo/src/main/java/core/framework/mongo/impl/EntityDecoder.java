package core.framework.mongo.impl;

import org.bson.BsonReader;

/**
 * @author neo
 */
@FunctionalInterface
public interface EntityDecoder<T> {
    T decode(BsonReader reader);
}
