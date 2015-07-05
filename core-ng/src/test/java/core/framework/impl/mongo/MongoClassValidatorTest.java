package core.framework.impl.mongo;

import org.junit.Test;

/**
 * @author neo
 */
public class MongoClassValidatorTest {
    @Test
    public void validateEntityClass() {
        new MongoClassValidator(TestEntity.class).validateEntityClass();
    }

    @Test
    public void validateViewClass() {
        new MongoClassValidator(TestView.class).validateViewClass();
    }
}