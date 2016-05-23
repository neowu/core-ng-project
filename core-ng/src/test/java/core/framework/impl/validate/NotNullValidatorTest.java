package core.framework.impl.validate;

import core.framework.api.util.Lists;
import core.framework.api.validate.NotNull;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * @author neo
 */
public class NotNullValidatorTest {
    @Test
    public void validate() {
        Validator validator = new ValidatorBuilder(Bean.class, Field::getName).build();

        Bean bean = new Bean();
        bean.child = new ChildBean();
        bean.children = Lists.newArrayList(bean.child);
        bean.optionalChild = Optional.of(new ChildBean());

        ValidationErrors errors = new ValidationErrors();
        validator.validate(bean, errors, false);

        Assert.assertTrue(errors.hasError());
        assertEquals(5, errors.errors.size());
        assertThat(errors.errors.get("stringField"), containsString("stringField"));
        assertThat(errors.errors.get("booleanField"), containsString("booleanField"));
        assertThat(errors.errors.get("child.intField"), containsString("intField"));
        assertThat(errors.errors.get("optionalChild.intField"), containsString("intField"));
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

    static class Bean {
        @NotNull(message = "stringField must not be null")
        public String stringField;
        public String nullStringField;
        @NotNull(message = "booleanField must not be null")
        public Boolean booleanField;
        @NotNull
        public ChildBean child;
        public List<ChildBean> children;
        @NotNull
        public Optional<ChildBean> optionalChild;
    }

    static class ChildBean {
        @NotNull(message = "intField must not be null")
        public Integer intField;
    }
}