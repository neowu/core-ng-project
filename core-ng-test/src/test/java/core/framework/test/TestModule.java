package core.framework.test;

import core.framework.api.AbstractTestModule;
import core.framework.test.mongo.TestEntity;

/**
 * @author neo
 */
public class TestModule extends AbstractTestModule {
    @Override
    protected void initialize() {
        mongo().uri("mongodb://localhost/test");
        mongo().entityClass(TestEntity.class);
    }
}
