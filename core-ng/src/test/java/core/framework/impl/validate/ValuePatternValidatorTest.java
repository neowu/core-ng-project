package core.framework.impl.validate;

import core.framework.api.util.Lists;
import core.framework.api.util.Maps;
import core.framework.api.validate.ValueNotNull;
import core.framework.api.validate.ValuePattern;
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
public class ValuePatternValidatorTest {
    @Test
    public void validate() {
        Validator validator = new ValidatorBuilder(Bean.class, Field::getName).build();

        Bean bean = new Bean();
        bean.field1 = Lists.newArrayList("a001");
        bean.field2 = Maps.newHashMap("key", "A001");

        ValidationErrors errors = new ValidationErrors();
        validator.validate(bean, errors, false);

        Assert.assertTrue(errors.hasError());
        assertEquals(1, errors.errors.size());
        assertThat(errors.errors.get("field2"), containsString("field2"));
    }

    static class Bean {
        @ValueNotNull
        @ValuePattern("[a-z0-9]{0,20}")
        public List<String> field1;

        @ValueNotNull
        @ValuePattern(value = "[a-z0-9]+", message = "field2 must be [a-z0-9]+")
        public Map<String, String> field2;
    }
}