package core.framework.module;

import core.framework.impl.module.ModuleContext;
import core.framework.impl.mongo.MongoImpl;
import core.framework.test.mongo.MockMongo;

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
