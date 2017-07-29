package core.framework.impl.db;

import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author neo
 */
public class DatabaseClassValidatorTest {
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void validateEntityClass() {
        new DatabaseClassValidator(AssignedIdEntity.class).validateEntityClass();
        new DatabaseClassValidator(AutoIncrementIdEntity.class).validateEntityClass();
        new DatabaseClassValidator(CompositeKeyEntity.class).validateEntityClass();
        new DatabaseClassValidator(SequenceIdEntity.class).validateEntityClass();
    }
}
