package core.framework.mongo.module;

import core.framework.mongo.impl.MockMongo;
import core.framework.mongo.impl.MongoImpl;

/**
 * @author neo
 */
public class TestMongoConfig extends MongoConfig {
    @Override
    MongoImpl createMongo() {
        return new MockMongo();
    }
}
