package core.framework.impl.mongo;

import org.junit.jupiter.api.Test;

/**
 * @author neo
 */
class MongoClassValidatorTest {
    @Test
    void validateEntityClass() {
        new MongoClassValidator(TestEntity.class).validateEntityClass();
    }

    @Test
    void validateViewClass() {
        new MongoClassValidator(TestView.class).validateViewClass();
    }
}
