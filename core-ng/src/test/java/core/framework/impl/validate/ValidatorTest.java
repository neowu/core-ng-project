package core.framework.impl.validate;

import core.framework.api.util.Lists;
import core.framework.api.util.Types;
import core.framework.api.validate.Length;
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
public class ValidatorTest {
    public static class Bean {
        @NotNull
        @Length(max = 5, message = "stringField must not be longer than 5")
        public String stringField;
        @Length(min = 5, message = "optionalStringField must be longer than 5")
        public String optionalStringField;
        @NotNull(message = "booleanField must not be null")
        public Boolean booleanField;
        @NotNull
        public ChildBean child;
        @NotNull
        public List<ChildBean> children;
    }

    public static class ChildBean {
        @NotNull(message = "intField must not be null")
        public Integer intField;
    }

    @Test
    public void validate() {
        Validator validator = new ValidatorBuilder(Bean.class, Field::getName).build();

        Bean instance = new Bean();
        instance.stringField = "123456";
        instance.child = new ChildBean();
        instance.children = Lists.newArrayList();
        instance.children.add(instance.child);

        ValidationResult result = validator.validate(instance);

        Assert.assertFalse(result.isValid());

        Map<String, String> errors = result.errors;

        assertEquals(4, errors.size());
        assertThat(errors.get("stringField"), containsString("stringField"));
        assertThat(errors.get("booleanField"), containsString("booleanField"));
        assertThat(errors.get("child.intField"), containsString("intField"));
        assertThat(errors.get("children.intField"), containsString("intField"));
    }

    @Test
    public void validateList() {
        Validator validator = new ValidatorBuilder(Types.list(Bean.class), Field::getName).build();

        Bean instance = new Bean();
        instance.stringField = "123456";
        instance.child = new ChildBean();
        instance.children = Lists.newArrayList();
        instance.children.add(instance.child);

        ValidationResult result = validator.validate(Lists.newArrayList(instance));

        Assert.assertFalse(result.isValid());

        Map<String, String> errors = result.errors;

        assertEquals(4, errors.size());
        assertThat(errors.get("stringField"), containsString("stringField"));
        assertThat(errors.get("booleanField"), containsString("booleanField"));
        assertThat(errors.get("child.intField"), containsString("intField"));
        assertThat(errors.get("children.intField"), containsString("intField"));
    }
}