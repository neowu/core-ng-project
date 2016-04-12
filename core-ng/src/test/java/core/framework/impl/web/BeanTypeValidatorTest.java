package core.framework.impl.web;

import core.framework.api.util.Types;
import org.junit.Test;

/**
 * @author neo
 */
public class BeanTypeValidatorTest {
    @Test
    public void validateListType() {
        new BeanTypeValidator(Types.list(String.class)).validate();
        new BeanTypeValidator(Types.list(TestBean.class)).validate();
    }

    @Test
    public void validateOptionalType() {
        new BeanTypeValidator(Types.optional(TestBean.class)).validate();
    }

    @Test
    public void validate() {
        new BeanTypeValidator(TestBean.class).validate();
    }
}