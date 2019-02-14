package core.framework.impl.web.bean;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class BeanBodyClassValidatorTest {
    @Test
    void validate() {
        var registry = new BeanBodyMapperRegistry();
        new BeanBodyClassValidator(TestBean.class, registry).validate();

        assertThatThrownBy(() -> new BeanBodyClassValidator(List.class, registry).validate())
                .isInstanceOf(Error.class)
                .hasMessageContaining("class must be bean class");
    }
}
