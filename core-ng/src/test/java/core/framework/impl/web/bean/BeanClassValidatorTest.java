package core.framework.impl.web.bean;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class BeanClassValidatorTest {
    @Test
    void validate() {
        var registry = new BeanMapperRegistry();
        new BeanClassValidator(TestBean.class, registry).validate();
    }

    @Test
    void withTopLevelList() {
        var registry = new BeanMapperRegistry();
        assertThatThrownBy(() -> new BeanClassValidator(List.class, registry).validate())
                .isInstanceOf(Error.class)
                .hasMessageContaining("top level list is not allowed");
    }
}
