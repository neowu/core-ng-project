package core.framework.internal.validate;

import core.framework.api.validate.NotNull;
import core.framework.api.validate.Size;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class BeanValidatorSizeTest {
    private BeanValidator validator;

    @BeforeEach
    void createBeanValidator() {
        validator = new BeanValidatorBuilder(Bean.class).build();
    }

    @Test
    void validate() {
        var bean = new Bean();
        bean.stringList = List.of("1", "2", "3", "4");
        bean.stringMap = Map.of();

        var errors = new ValidationErrors();
        validator.validate(bean, errors, false);

        assertThat(errors.hasError()).isTrue();
        assertThat(errors.errors).hasSize(2);
        assertThat(errors.errors.get("stringList")).isEqualTo("stringList must not have more than 3 items, value=4");
        assertThat(errors.errors.get("stringMap")).isEqualTo("size must be between 1 and inf, size=0");
    }

    @Test
    void validateWithoutError() {
        var bean = new Bean();
        bean.stringList = List.of("1", "2", "3");
        bean.stringMap = Map.of("key", "value");
        bean.children = List.of(new Child());
        bean.stringListMap = Map.of("k1", List.of("v1"), "k2", List.of("v2"));

        var errors = new ValidationErrors();
        validator.validate(bean, errors, false);

        assertThat(errors.hasError()).isFalse();
    }

    static class Bean {
        @NotNull
        @Size(max = 3, message = "stringList must not have more than {max} items, value={value}")
        public List<String> stringList;

        @NotNull
        @Size(min = 1)
        public Map<String, String> stringMap;

        @Size(min = 1, max = 3, message = "children must have 1 to 3 items")
        public List<Child> children;

        @Size(min = 2, max = 3, message = "stringListMap must have 2 to 3 items")
        public Map<String, List<String>> stringListMap;
    }

    static class Child {
        public Integer intField;
    }
}
