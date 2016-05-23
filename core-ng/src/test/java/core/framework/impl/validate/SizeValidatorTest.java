package core.framework.impl.validate;

import core.framework.api.util.Lists;
import core.framework.api.util.Maps;
import core.framework.api.validate.NotNull;
import core.framework.api.validate.Size;
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
public class SizeValidatorTest {
    @Test
    public void validate() {
        Validator validator = new ValidatorBuilder(Bean.class, Field::getName).build();

        Bean bean = new Bean();
        bean.stringList = Lists.newArrayList("1", "2", "3", "4");
        bean.stringMap = Maps.newHashMap();

        ValidationErrors errors = new ValidationErrors();
        validator.validate(bean, errors, false);

        Assert.assertTrue(errors.hasError());
        assertEquals(2, errors.errors.size());
        assertThat(errors.errors.get("stringList"), containsString("stringList"));
        assertThat(errors.errors.get("stringMap"), containsString("stringMap"));
    }

    static class Bean {
        @NotNull
        @Size(max = 3, message = "stringList must not have more than 3 items")
        public List<String> stringList;

        @NotNull
        @Size(min = 1, message = "stringMap must have at least 1 item")
        public Map<String, String> stringMap;

    }
}