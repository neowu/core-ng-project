package core.framework.impl.validate.v2;

import core.framework.api.util.Lists;
import core.framework.api.util.Maps;
import core.framework.api.validate.NotNull;
import core.framework.api.validate.Size;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author neo
 */
public class ObjectValidatorSizeTest {
    ObjectValidator validator;

    @Before
    public void createObjectValidator() {
        validator = new ObjectValidatorBuilder(Bean.class, Field::getName).build().get();
    }

    @Test
    public void validate() {
        Bean bean = new Bean();
        bean.stringList = Lists.newArrayList("1", "2", "3", "4");
        bean.stringMap = Maps.newHashMap();

        ValidationErrors errors = new ValidationErrors();
        validator.validate(bean, errors, false);

        assertTrue(errors.hasError());
        assertEquals(2, errors.errors.size());
        assertThat(errors.errors.get("stringList"), containsString("stringList"));
        assertThat(errors.errors.get("stringMap"), containsString("stringMap"));
    }

    @Test
    public void validateWithoutError() {
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
