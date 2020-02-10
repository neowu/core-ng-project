package core.framework.internal.db;

import core.framework.api.json.Property;
import core.framework.api.validate.NotNull;
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

    @Test
    void withNotNullAnnotationPrimaryKey() {
        assertThatThrownBy(() -> new DatabaseClassValidator(TestEntityWithNotNullAnnotationPK.class).validateEntityClass())
                .isInstanceOf(Error.class)
                .hasMessageContaining("db @PrimaryKey field must not have @NotNull");
    }

    @Test
    void withPropertyAnnotation() {
        assertThatThrownBy(() -> new DatabaseClassValidator(TestEntityWithPropertyAnnotation.class).validateEntityClass())
                .isInstanceOf(Error.class)
                .hasMessageContaining("db entity field must not have json annotation");
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

    @Table(name = "test_entity_with_not_null_annotation_pk")
    public static class TestEntityWithNotNullAnnotationPK {
        @NotNull
        @PrimaryKey
        @Column(name = "id")
        public String id;
    }

    @Table(name = "test_entity_with_property_annotation")
    public static class TestEntityWithPropertyAnnotation {
        @PrimaryKey
        @Column(name = "id")
        @Property(name = "id")
        public String id;
    }
}
