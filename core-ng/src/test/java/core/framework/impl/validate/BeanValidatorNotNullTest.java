package core.framework.impl.validate;

import core.framework.api.validate.NotNull;
import core.framework.util.ClasspathResources;
import core.framework.util.Lists;
import core.framework.util.Maps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author neo
 */
class BeanValidatorNotNullTest {
    private BeanValidatorBuilder builder;
    private BeanValidator validator;

    @BeforeEach
    void createObjectValidator() {
        builder = new BeanValidatorBuilder(Bean.class, Field::getName);
        validator = builder.build().orElseThrow();
    }

    @Test
    void sourceCode() {
        String sourceCode = builder.builder.sourceCode();
        assertEquals(ClasspathResources.text("validator-test/validator-notnull.java"), sourceCode);
    }

    @Test
    void validate() {
        Bean bean = new Bean();
        bean.child = new ChildBean();
        bean.children = Lists.newArrayList(bean.child);
        bean.childMap = Maps.newHashMap("child1", bean.child);

        ValidationErrors errors = new ValidationErrors();
        validator.validate(bean, errors, false);

        assertTrue(errors.hasError());
        assertEquals(5, errors.errors.size());
        assertThat(errors.errors.get("stringField")).contains("stringField");
        assertThat(errors.errors.get("booleanField")).contains("booleanField");
        assertThat(errors.errors.get("child.intField")).contains("intField");
        assertThat(errors.errors.get("children.intField")).contains("intField");
        assertThat(errors.errors.get("childMap.intField")).contains("intField");
    }

    @Test
    void partialValidate() {
        Bean bean = new Bean();

        ValidationErrors errors = new ValidationErrors();
        validator.validate(bean, errors, true);

        assertFalse(errors.hasError());
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
        public Map<String, ChildBean> childMap;
    }

    static class ChildBean {
        @NotNull(message = "intField must not be null")
        public Integer intField;
    }
}
