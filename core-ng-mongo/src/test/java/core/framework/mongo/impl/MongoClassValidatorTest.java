package core.framework.mongo.impl;

import core.framework.api.json.Property;
import core.framework.api.validate.NotNull;
import core.framework.mongo.Field;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

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

    @Test
    void validateViewClassWithJSONAnnotation() {
        assertThatThrownBy(() -> new MongoClassValidator(TestViewWithJSONProperty.class).validateViewClass())
            .isInstanceOf(Error.class)
            .hasMessageContaining("mongo entity field must not have json annotation");
    }

    public static class TestViewWithJSONProperty {
        @NotNull
        @Field(name = "int_field")
        @Property(name = "int_field")
        public Integer intField;
    }
}
