package core.framework.impl.mongo;

import org.bson.BsonReader;

/**
 * @author neo
 */
@FunctionalInterface
public interface EntityDecoder<T> {
    T decode(BsonReader reader);
}
