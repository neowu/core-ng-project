package core.framework.impl.mongo;

import org.bson.BsonReader;

/**
 * @author neo
 */
public interface EntityDecoder<T> {
    T decode(BsonReader reader);
}
