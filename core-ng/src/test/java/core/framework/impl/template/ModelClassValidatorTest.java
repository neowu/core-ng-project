package core.framework.impl.template;

import org.junit.jupiter.api.Test;

/**
 * @author neo
 */
class ModelClassValidatorTest {
    @Test
    void validate() {
        new ModelClassValidator(TestModel.class).validate();
    }
}
