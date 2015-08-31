package core.framework.impl.validate;

import core.framework.api.util.Lists;
import core.framework.api.util.Types;
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
public class NotNullValidatorTest {
    static class TestBean {
        @NotNull(message = "stringField must not be null")
        public String stringField;
        public String optionalStringField;
        @NotNull(message = "booleanField must not be null")
        public Boolean booleanField;
        @NotNull
        public ChildBean child;
        @NotNull
        public List<ChildBean> children;
    }

    static class ChildBean {
        @NotNull(message = "intField must not be null")
        public Integer intField;
    }

    @Test
    public void validate() {
        Validator validator = new ValidatorBuilder(TestBean.class, Field::getName).build();

        TestBean instance = new TestBean();
        instance.child = new ChildBean();
        instance.children = Lists.newArrayList();
        instance.children.add(instance.child);

        ValidationErrors validationErrors = new ValidationErrors();
        validator.validate(instance, validationErrors);

        Assert.assertTrue(validationErrors.hasError());

        Map<String, String> errors = validationErrors.errors;

        assertEquals(4, errors.size());
        assertThat(errors.get("stringField"), containsString("stringField"));
        assertThat(errors.get("booleanField"), containsString("booleanField"));
        assertThat(errors.get("child.intField"), containsString("intField"));
        assertThat(errors.get("children.intField"), containsString("intField"));
    }

    @Test
    public void validateList() {
        Validator validator = new ValidatorBuilder(Types.list(TestBean.class), Field::getName).build();

        TestBean instance = new TestBean();
        instance.child = new ChildBean();
        instance.children = Lists.newArrayList();
        instance.children.add(instance.child);

        ValidationErrors validationErrors = new ValidationErrors();
        validator.validate(Lists.newArrayList(instance), validationErrors);

        Assert.assertTrue(validationErrors.hasError());

        Map<String, String> errors = validationErrors.errors;

        assertEquals(4, errors.size());
        assertThat(errors.get("stringField"), containsString("stringField"));
        assertThat(errors.get("booleanField"), containsString("booleanField"));
        assertThat(errors.get("child.intField"), containsString("intField"));
        assertThat(errors.get("children.intField"), containsString("intField"));
    }
}