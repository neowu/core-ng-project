package core.framework.mongo.module;

import core.framework.impl.module.ModuleContext;
import core.framework.mongo.impl.MockMongo;
import core.framework.mongo.impl.MongoImpl;

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
