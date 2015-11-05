package core.framework.impl.validate;

import core.framework.api.util.Lists;
import core.framework.api.util.Maps;
import core.framework.api.validate.ValueNotNull;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * @author neo
 */
public class ValueNotNullValidatorTest {
    static class Bean {
        @ValueNotNull(message = "field1 must not contain null")
        public List<ChildBean> field1;

        @ValueNotNull(message = "field2 must not contain null")
        public List<String> field2;

        @ValueNotNull(message = "field3 must not contain null")
        public Map<String, ChildBean> field3;
    }

    private static class ChildBean {
    }

    @Test
    public void validate() {
        Validator validator = new ValidatorBuilder(Bean.class, Field::getName).build();

        Bean bean = new Bean();
        bean.field1 = Lists.newArrayList(null, null);
        bean.field2 = Lists.newArrayList(null, null);
        bean.field3 = Maps.newHashMap("key", null);

        ValidationErrors errors = new ValidationErrors();
        validator.validate(bean, errors, false);

        Assert.assertTrue(errors.hasError());
        assertEquals(3, errors.errors.size());
        assertThat(errors.errors.get("field1"), containsString("field1"));
        assertThat(errors.errors.get("field2"), containsString("field2"));
        assertThat(errors.errors.get("field3"), containsString("field3"));
    }
}