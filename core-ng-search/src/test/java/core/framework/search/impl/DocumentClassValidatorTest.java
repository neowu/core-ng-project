package core.framework.search.impl;

import core.framework.api.json.Property;
import core.framework.api.validate.NotNull;
import core.framework.search.Index;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class DocumentClassValidatorTest {
    @Test
    void validate() {
        new DocumentClassValidator(TestDocument.class).validate();
    }

    @Test
    void validateWithoutIndexAnnotation() {
        assertThatThrownBy(() -> new DocumentClassValidator(TestDocumentWithoutIndexAnnotation.class).validate())
            .isInstanceOf(Error.class)
            .hasMessageContaining("document class must have @Index");
    }

    @Test
    void validateWithDefaultValue() {
        assertThatThrownBy(() -> new DocumentClassValidator(TestDocumentWithDefaultValue.class).validate())
            .isInstanceOf(Error.class)
            .hasMessageContaining("document field must not have default value");
    }

    @Index(name = "test")
    public static class TestDocument {
        @Property(name = "date_time_field")
        public LocalDateTime dateTimeField;

        @NotNull
        @Property(name = "string_field")
        public String stringField;

        @Property(name = "list_field")
        public List<String> listField;

        @Property(name = "map_field")
        public Map<String, String> mapField;
    }

    public static class TestDocumentWithoutIndexAnnotation {
    }

    @Index(name = "test")
    public static class TestDocumentWithDefaultValue {
        @Property(name = "child")
        public Child child;

        @Property(name = "list_field")
        public List<String> listField = List.of();
    }

    public static class Child {
        @NotNull
        @Property(name = "string_field")
        public String stringField;
    }
}
