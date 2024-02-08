package core.framework.internal.db;

import core.framework.api.json.Property;
import core.framework.api.validate.NotNull;
import core.framework.db.Column;
import core.framework.db.PrimaryKey;
import core.framework.db.Table;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class DatabaseClassValidatorTest {
    @Test
    void validateEntityClass() {
        new DatabaseClassValidator(AssignedIdEntity.class, false).validate();
        new DatabaseClassValidator(AutoIncrementIdEntity.class, false).validate();
        new DatabaseClassValidator(CompositeKeyEntity.class, false).validate();
        new DatabaseClassValidator(JSONEntity.class, false).validate();
    }

    @Test
    void withInvalidPrimaryKeyType() {
        assertThatThrownBy(() -> new DatabaseClassValidator(TestEntityWithInvalidPrimaryKey.class, false).validate())
            .isInstanceOf(Error.class)
            .hasMessageContaining("primary key must be Integer or Long");
    }

    @Test
    void withDefaultValue() {
        assertThatThrownBy(() -> new DatabaseClassValidator(TestEntityWithDefaultValue.class, false).validate())
            .isInstanceOf(Error.class)
            .hasMessageContaining("db entity field must not have default value");
    }

    @Test
    void viewWithPrimaryKey() {
        assertThatThrownBy(() -> new DatabaseClassValidator(TestViewWithPrimaryKey.class, true).validate())
            .isInstanceOf(Error.class)
            .hasMessageContaining("db view field must not have @PrimaryKey");
    }

    @Test
    void withNotNullAnnotationPrimaryKey() {
        assertThatThrownBy(() -> new DatabaseClassValidator(TestEntityWithNotNullAnnotationPK.class, false).validate())
            .isInstanceOf(Error.class)
            .hasMessageContaining("db @PrimaryKey field must not have @NotNull");
    }

    @Test
    void withPropertyAnnotation() {
        assertThatThrownBy(() -> new DatabaseClassValidator(TestEntityWithPropertyAnnotation.class, false).validate())
            .isInstanceOf(Error.class)
            .hasMessageContaining("db entity field must not have json annotation");
    }

    @Test
    void withJSON() {
        new DatabaseClassValidator(TestEntityWithJSON.class, false).validate();
    }

    @Test
    void withInvalidJSONList() {
        assertThatThrownBy(() -> new DatabaseClassValidator(TestEntityWithInvalidJSONList.class, false).validate())
            .isInstanceOf(Error.class)
            .hasMessageContaining("db json list field must be List<T> and T must be enum or value class");
    }

    @Test
    void withInvalidJSON() {
        assertThatThrownBy(() -> new DatabaseClassValidator(TestEntityWithInvalidJSON.class, false).validate())
            .isInstanceOf(Error.class)
            .hasMessageContaining("db json field must be bean or list");
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

    @Table(name = "test_entity_with_json")
    public static class TestEntityWithJSON {
        @PrimaryKey
        @Column(name = "id")
        public String id;

        @Column(name = "json", json = true)
        public TestJSON json;
    }

    public static class TestJSON {
        @Property(name = "data")
        public String data;
    }

    @Table(name = "test_entity_with_invalid_json_list")
    public static class TestEntityWithInvalidJSONList {
        @PrimaryKey
        @Column(name = "id")
        public String id;

        @Column(name = "list", json = true)
        public List<TestJSON> list;
    }

    @Table(name = "test_entity_with_invalid_json")
    public static class TestEntityWithInvalidJSON {
        @PrimaryKey
        @Column(name = "id")
        public String id;

        @Column(name = "stringField", json = true)
        public String stringField;
    }
}
