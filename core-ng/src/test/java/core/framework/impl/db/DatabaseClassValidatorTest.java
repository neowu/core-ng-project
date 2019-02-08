package core.framework.impl.db;

import core.framework.db.Column;
import core.framework.db.PrimaryKey;
import core.framework.db.Table;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    void withInvalidPrimaryKeyType() {
        assertThatThrownBy(() -> new DatabaseClassValidator(TestEntityWithInvalidPrimaryKey.class).validateEntityClass())
                .isInstanceOf(Error.class)
                .hasMessageContaining("primary key must be Integer or Long");
    }

    @Test
    void withDefaultValue() {
        assertThatThrownBy(() -> new DatabaseClassValidator(TestEntityWithDefaultValue.class).validateEntityClass())
                .isInstanceOf(Error.class)
                .hasMessageContaining("db entity field must not have default value");
    }

    @Test
    void viewWithPrimaryKey() {
        assertThatThrownBy(() -> new DatabaseClassValidator(TestViewWithPrimaryKey.class).validateViewClass())
                .isInstanceOf(Error.class)
                .hasMessageContaining("db view field must not have @PrimaryKey");
    }

    @Table(name = "test_entity_with_invalid_pk")
    public static class TestEntityWithInvalidPrimaryKey {
        @PrimaryKey(autoIncrement = true)
        @Column(name = "id")
        public String id;
    }

    @Table(name = "test_entity_with_default_value")
    public static class TestEntityWithDefaultValue {
        @PrimaryKey
        @Column(name = "id")
        public String id;

        @Column(name = "name")
        public String name = "default";
    }

    public static class TestViewWithPrimaryKey {
        @PrimaryKey(autoIncrement = true)
        @Column(name = "id")
        public Integer id;
    }
}
