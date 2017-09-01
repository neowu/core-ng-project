package core.framework.impl.validate;

import core.framework.api.util.Lists;
import core.framework.api.util.Maps;
import core.framework.api.util.Types;
import core.framework.api.validate.NotEmpty;
import core.framework.api.validate.NotNull;
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
public class ObjectValidatorListTest {
    ObjectValidator validator;

    @Before
    public void createObjectValidator() {
        validator = new ObjectValidatorBuilder(Types.list(Bean.class), Field::getName).build().get();
    }

    @Test
    public void validate() {
        Bean bean = new Bean();
        bean.field1 = "";
        bean.child = new ChildBean();
        bean.child.field3 = " ";
        bean.children = Lists.newArrayList(new ChildBean());
        bean.childMap = Maps.newHashMap("key1", new ChildBean());

        ValidationErrors errors = new ValidationErrors();
        validator.validate(Lists.newArrayList(bean), errors, false);

        assertTrue(errors.hasError());
        assertEquals(4, errors.errors.size());
        assertThat(errors.errors.get("field1"), containsString("field1"));
        assertThat(errors.errors.get("child.field3"), containsString("field3"));
        assertThat(errors.errors.get("children.field3"), containsString("field3"));
        assertThat(errors.errors.get("childMap.field3"), containsString("field3"));
    }

    static class Bean {
        @NotNull
        @NotEmpty(message = "field1 must not be empty")
        public String field1;

        @NotEmpty(message = "field2 must not be empty")
        public String field2;

        public ChildBean child;

        public List<ChildBean> children;

        public Map<String, ChildBean> childMap;
    }

    static class ChildBean {
        @NotNull(message = "field3 must not be null")
        @NotEmpty(message = "field3 must not be empty")
        public String field3;
    }
}
