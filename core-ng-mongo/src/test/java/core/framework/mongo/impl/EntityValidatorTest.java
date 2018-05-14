package core.framework.mongo.impl;

import org.junit.jupiter.api.Test;

/**
 * @author neo
 */
class EntityValidatorTest {
    @Test
    void validate() {
        EntityValidator<TestEntity> validator = new EntityValidator<>(TestEntity.class);
        TestEntity entity = new TestEntity();
        entity.intField = 3;
        validator.validate(entity);
    }
}
