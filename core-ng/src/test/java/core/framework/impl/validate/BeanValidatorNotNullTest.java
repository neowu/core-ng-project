package core.framework.impl.validate;

import core.framework.api.validate.NotNull;
import core.framework.util.ClasspathResources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class BeanValidatorNotNullTest {
    private BeanValidatorBuilder builder;
    private BeanValidator validator;

    @BeforeEach
    void createObjectValidator() {
        builder = new BeanValidatorBuilder(Bean.class, Field::getName);
        validator = builder.build().orElseThrow();
    }

    @Test
    void sourceCode() {
        assertThat(builder.builder.sourceCode())
                .isEqualTo(ClasspathResources.text("validator-test/validator-not-null.java"));
    }

    @Test
    void validate() {
        var bean = new Bean();
        bean.child = new Child();
        bean.children = List.of(bean.child);
        bean.childMap = Map.of("child1", bean.child);

        var errors = new ValidationErrors();
        validator.validate(bean, errors, false);

        assertThat(errors.hasError()).isTrue();
        assertThat(errors.errors)
                .containsOnlyKeys("stringField", "booleanField", "child.intField", "children.intField", "childMap.intField");
    }

    @Test
    void partialValidate() {
        var bean = new Bean();

        var errors = new ValidationErrors();
        validator.validate(bean, errors, true);

        assertThat(errors.hasError()).isFalse();
    }

    static class Bean {
        @NotNull(message = "stringField must not be null")
        public String stringField;
        public String nullStringField;
        @NotNull(message = "booleanField must not be null")
        public Boolean booleanField;
        @NotNull
        public Child child;
        public List<Child> children;
        @NotNull
        public Map<String, Child> childMap;
    }

    static class Child {
        @NotNull(message = "intField must not be null")
        public Integer intField;
    }
}
