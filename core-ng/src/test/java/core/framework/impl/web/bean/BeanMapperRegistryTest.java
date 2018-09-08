package core.framework.impl.web.bean;

import core.framework.impl.reflect.Classes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class BeanMapperRegistryTest {
    private BeanMapperRegistry registry;

    @BeforeEach
    void createBeanMapperRegistry() {
        registry = new BeanMapperRegistry();
    }

    @Test
    void validateBeanClassName() {
        registry.beanClasses.put(Classes.className(TestBean.class), Void.class);

        assertThatThrownBy(() -> registry.validateBeanClassName(TestBean.class))
                .isInstanceOf(Error.class).hasMessageContaining("found bean class with duplicate name");
    }

    public static class TestBean {
    }
}
