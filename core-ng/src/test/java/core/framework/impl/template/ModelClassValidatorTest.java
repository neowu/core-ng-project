package core.framework.impl.template;

import org.junit.Test;

/**
 * @author neo
 */
public class ModelClassValidatorTest {
    @Test
    public void validate() {
        new ModelClassValidator(TestModel.class).validate();
    }
}