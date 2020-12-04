package core.framework.internal.web.bean;

import core.framework.internal.bean.BeanClassValidator;
import core.framework.internal.bean.TestBean;
import core.framework.web.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

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
    void register() {
        reader.registerBean(TestBean.class, validator);
        reader.registerBean(TestBean.class, validator);
    }

    @Test
    void registerQueryParam() {
        reader.registerQueryParam(TestQueryParamBean.class, validator.beanClassNameValidator);
        reader.registerQueryParam(TestQueryParamBean.class, validator.beanClassNameValidator);
    }

    @Test
    void fromJSON() {
        assertThatThrownBy(() -> reader.fromJSON(String.class, new byte[0]))
                .isInstanceOf(Error.class)
                .hasMessageContaining("bean class must not be java built-in class");

        assertThatThrownBy(() -> reader.fromJSON(TestQueryParamBean.class, new byte[0]))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("content-type not supported");
    }

    @Test
    void fromQueryParam() {
        assertThatThrownBy(() -> reader.fromParams(TestBean.class, Map.of()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("content-type not supported");

        assertThatThrownBy(() -> reader.fromParams(TestQueryParamBean.class, Map.of()))
                .isInstanceOf(Error.class)
                .hasMessageContaining("bean class is not registered");
    }
}
