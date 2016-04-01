package core.framework.api.mongo;

import org.bson.Document;

/**
 * @author neo
 */
public interface Mongo {
    void dropCollection(String collection);

    Document eval(String function, Object... arguments);
}
