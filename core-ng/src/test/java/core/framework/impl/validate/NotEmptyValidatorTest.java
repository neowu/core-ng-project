package core.framework.impl.validate;

import core.framework.api.util.Lists;
import core.framework.api.util.Maps;
import core.framework.api.validate.NotEmpty;
import core.framework.api.validate.NotNull;
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
public class NotEmptyValidatorTest {
    @Test
    public void validate() {
        Validator validator = new ValidatorBuilder(Bean.class, Field::getName).build();

        Bean bean = new Bean();
        bean.stringField1 = "";
        bean.stringList = Lists.newArrayList("");
        bean.stringMap = Maps.newHashMap("key", "");

        ValidationErrors errors = new ValidationErrors();
        validator.validate(bean, errors, false);

        Assert.assertTrue(errors.hasError());
        assertEquals(3, errors.errors.size());
        assertThat(errors.errors.get("stringField1"), containsString("stringField1"));
        assertThat(errors.errors.get("stringList"), containsString("stringList"));
        assertThat(errors.errors.get("stringMap"), containsString("stringMap"));
    }

    static class Bean {
        @NotNull
        @NotEmpty(message = "stringField1 must not be empty")
        public String stringField1;

        @NotEmpty(message = "stringField2 must not be empty")
        public String stringField2;

        @NotEmpty(message = "stringList must not contain empty")
        public List<String> stringList;

        @NotEmpty(message = "stringMap must not contain empty")
        public Map<String, String> stringMap;
    }
}