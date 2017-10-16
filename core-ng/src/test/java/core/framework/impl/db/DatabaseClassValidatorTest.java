package core.framework.impl.db;

import org.junit.jupiter.api.Test;

/**
 * @author neo
 */
class DatabaseClassValidatorTest {
    @Test
    void validateEntityClass() {
        new DatabaseClassValidator(AssignedIdEntity.class).validateEntityClass();
        new DatabaseClassValidator(AutoIncrementIdEntity.class).validateEntityClass();
        new DatabaseClassValidator(CompositeKeyEntity.class).validateEntityClass();
        new DatabaseClassValidator(SequenceIdEntity.class).validateEntityClass();
    }
}
