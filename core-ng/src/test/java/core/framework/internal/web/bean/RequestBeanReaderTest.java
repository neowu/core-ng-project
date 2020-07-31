package core.framework.internal.web.bean;

import core.framework.internal.bean.BeanClassValidator;
import core.framework.internal.bean.TestBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class RequestBeanReaderTest {
    private RequestBeanReader reader;
    private BeanClassValidator validator;

    @BeforeEach
    void createRequestBeanReader() {
        validator = new BeanClassValidator();
        reader = new RequestBeanReader();
    }

    @Test
    void fromJSON() {
        assertThatThrownBy(() -> reader.fromJSON(String.class, new byte[0]))
                .isInstanceOf(Error.class)
                .hasMessageContaining("bean class must not be java built-in class");
    }

    @Test
    void register() {
        reader.registerBean(TestBean.class, validator);
        reader.registerBean(TestBean.class, validator);
    }

    @Test
    void registerQueryParam() {
        reader.registerQueryParam(TestQueryParamBean.class, validator.beanClassNameValidator);
        reader.registerQueryParam(TestQueryParamBean.class, validator.beanClassNameValidator);
    }
}
