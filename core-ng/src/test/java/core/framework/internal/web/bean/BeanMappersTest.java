package core.framework.internal.web.bean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class BeanMappersTest {
    private BeanMappers mappers;

    @BeforeEach
    void createBeanMappers() {
        mappers = new BeanMappers();
    }

    @Test
    void mapper() {
        assertThatThrownBy(() -> mappers.mapper(String.class))
            .isInstanceOf(Error.class)
            .hasMessageContaining("bean class must not be java built-in class");
    }
}
