package core.framework.impl.validate;

import core.framework.api.util.Lists;
import core.framework.api.util.Types;
import core.framework.api.validate.NotNull;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * @author neo
 */
public class NotNullValidatorTest {
    static class Bean {
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
        Validator validator = new ValidatorBuilder(Bean.class, Field::getName).build();

        Bean bean = new Bean();
        bean.child = new ChildBean();
        bean.children = Lists.newArrayList();
        bean.children.add(bean.child);

        ValidationErrors errors = new ValidationErrors();
        validator.validate(bean, errors, false);

        Assert.assertTrue(errors.hasError());
        assertEquals(4, errors.errors.size());
        assertThat(errors.errors.get("stringField"), containsString("stringField"));
        assertThat(errors.errors.get("booleanField"), containsString("booleanField"));
        assertThat(errors.errors.get("child.intField"), containsString("intField"));
        assertThat(errors.errors.get("children.intField"), containsString("intField"));
    }

    @Test
    public void partialValidate() {
        Validator validator = new ValidatorBuilder(Bean.class, Field::getName).build();

        Bean bean = new Bean();

        ValidationErrors errors = new ValidationErrors();
        validator.validate(bean, errors, true);

        Assert.assertFalse(errors.hasError());
    }

    @Test
    public void validateList() {
        Validator validator = new ValidatorBuilder(Types.list(Bean.class), Field::getName).build();

        Bean bean = new Bean();
        bean.child = new ChildBean();
        bean.children = Lists.newArrayList();
        bean.children.add(bean.child);

        ValidationErrors errors = new ValidationErrors();
        validator.validate(Lists.newArrayList(bean), errors, false);

        Assert.assertTrue(errors.hasError());
        assertEquals(4, errors.errors.size());
        assertThat(errors.errors.get("stringField"), containsString("stringField"));
        assertThat(errors.errors.get("booleanField"), containsString("booleanField"));
        assertThat(errors.errors.get("child.intField"), containsString("intField"));
        assertThat(errors.errors.get("children.intField"), containsString("intField"));
    }
}