package core.framework.mongo.config;

import core.framework.impl.module.ModuleContext;
import core.framework.mongo.impl.MongoImpl;
import core.framework.mongo.test.MockMongo;

/**
 * @author neo
 */
public class TestMongoConfig extends MongoConfig {
    TestMongoConfig(ModuleContext context, String name) {
        super(context, name);
    }

    @Override
    MongoImpl createMongo() {
        return new MockMongo();
    }
}
