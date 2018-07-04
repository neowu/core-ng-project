package core.framework.impl.validate;

import core.framework.api.validate.NotNull;
import core.framework.api.validate.Size;
import core.framework.util.Lists;
import core.framework.util.Maps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        bean.stringList = Lists.newArrayList("1", "2", "3", "4");
        bean.stringMap = Maps.newHashMap();

        ValidationErrors errors = new ValidationErrors();
        validator.validate(bean, errors, false);

        assertTrue(errors.hasError());
        assertEquals(2, errors.errors.size());
        assertThat(errors.errors.get("stringList")).contains("stringList");
        assertThat(errors.errors.get("stringMap")).contains("stringMap");
    }

    @Test
    void validateWithoutError() {
        Bean bean = new Bean();
        bean.stringList = Lists.newArrayList("1", "2", "3");
        bean.stringMap = Maps.newHashMap("key", "value");
        bean.children = Lists.newArrayList(new ChildBean());

        ValidationErrors errors = new ValidationErrors();
        validator.validate(bean, errors, false);

        assertFalse(errors.hasError());
    }

    static class Bean {
        @NotNull
        @Size(max = 3, message = "stringList must not have more than 3 items")
        public List<String> stringList;

        @NotNull
        @Size(min = 1, message = "stringMap must have at least 1 item")
        public Map<String, String> stringMap;

        @Size(min = 1, max = 3, message = "children must have 1 to 3 items")
        public List<ChildBean> children;
    }

    static class ChildBean {
        public Integer intField;
    }
}
