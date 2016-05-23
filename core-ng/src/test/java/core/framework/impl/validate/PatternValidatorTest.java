package core.framework.impl.validate;

import core.framework.api.util.Lists;
import core.framework.api.util.Maps;
import core.framework.api.validate.NotNull;
import core.framework.api.validate.Pattern;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author neo
 */
public class PatternValidatorTest {
    Validator validator;

    @Before
    public void createValidator() {
        validator = new ValidatorBuilder(Bean.class, Field::getName).build();
    }

    @Test
    public void valid() {
        Bean bean = new Bean();
        bean.field1 = "abc-def";
        validator.validate(bean);
    }

    @Test
    public void invalid() {
        Bean bean = new Bean();
        bean.field1 = "ABC-DEF";
        bean.field2 = Lists.newArrayList("a001");
        bean.field3 = Maps.newHashMap("key", "A001");

        ValidationErrors errors = new ValidationErrors();
        validator.validate(bean, errors, false);

        assertTrue(errors.hasError());
        assertEquals(2, errors.errors.size());
        assertThat(errors.errors.get("field1"), containsString("field1"));
        assertThat(errors.errors.get("field3"), containsString("field3"));
    }

    static class Bean {
        @NotNull
        @Pattern(value = "[a-z-]+", message = "field1 must match pattern")
        public String field1;

        @Pattern("[a-z0-9]{0,20}")
        public List<String> field2;

        @Pattern(value = "[a-z0-9]+", message = "field3 must be [a-z0-9]+")
        public Map<String, String> field3;
    }
}