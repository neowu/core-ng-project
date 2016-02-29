package core.framework.impl.mongo;

import org.junit.Test;

/**
 * @author neo
 */
public class EntityClassValidatorTest {
    @Test
    public void validateEntityClass() {
        new EntityClassValidator(TestEntity.class).validateEntityClass();
    }

    @Test
    public void validateViewClass() {
        new EntityClassValidator(TestView.class).validateViewClass();
    }
}