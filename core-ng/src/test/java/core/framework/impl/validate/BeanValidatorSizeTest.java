package core.framework.impl.validate;

import core.framework.api.validate.NotNull;
import core.framework.api.validate.Size;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class BeanValidatorSizeTest {
    private BeanValidator validator;

    @BeforeEach
    void createObjectValidator() {
        validator = new BeanValidatorBuilder(Bean.class, Field::getName).build().orElseThrow();
    }

    @Test
    void validate() {
        Bean bean = new Bean();
        bean.stringList = List.of("1", "2", "3", "4");
        bean.stringMap = Map.of();

        ValidationErrors errors = new ValidationErrors();
        validator.validate(bean, errors, false);

        assertThat(errors.hasError()).isTrue();
        assertThat(errors.errors).hasSize(2);
        assertThat(errors.errors.get("stringList")).contains("stringList");
        assertThat(errors.errors.get("stringMap")).contains("stringMap");
    }

    @Test
    void validateWithoutError() {
        Bean bean = new Bean();
        bean.stringList = List.of("1", "2", "3");
        bean.stringMap = Map.of("key", "value");
        bean.children = List.of(new Child());

        ValidationErrors errors = new ValidationErrors();
        validator.validate(bean, errors, false);

        assertThat(errors.hasError()).isFalse();
    }

    static class Bean {
        @NotNull
        @Size(max = 3, message = "stringList must not have more than 3 items")
        public List<String> stringList;

        @NotNull
        @Size(min = 1, message = "stringMap must have at least 1 item")
        public Map<String, String> stringMap;

        @Size(min = 1, max = 3, message = "children must have 1 to 3 items")
        public List<Child> children;
    }

    static class Child {
        public Integer intField;
    }
}
