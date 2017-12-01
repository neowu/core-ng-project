package core.framework.impl.validate;

import core.framework.api.validate.NotEmpty;
import core.framework.api.validate.NotNull;
import core.framework.util.ClasspathResources;
import core.framework.util.Lists;
import core.framework.util.Maps;
import core.framework.util.Types;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author neo
 */
class ObjectValidatorListTest {
    private ObjectValidatorBuilder builder;
    private ObjectValidator validator;

    @BeforeEach
    void createObjectValidator() {
        builder = new ObjectValidatorBuilder(Types.list(Bean.class), Field::getName);
        validator = builder.build().get();
    }

    @Test
    void sourceCode() {
        String sourceCode = builder.builder.sourceCode();
        assertEquals(ClasspathResources.text("validator-test/validator-list.java"), sourceCode);
    }

    @Test
    void validate() {
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
        assertThat(errors.errors.get("field1")).contains("field1");
        assertThat(errors.errors.get("child.field3")).contains("field3");
        assertThat(errors.errors.get("children.field3")).contains("field3");
        assertThat(errors.errors.get("childMap.field3")).contains("field3");
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
