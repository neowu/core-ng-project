package core.framework.impl.web.service;

import core.framework.impl.web.bean.BeanClassNameValidator;
import core.framework.impl.web.bean.RequestBeanMapper;
import core.framework.impl.web.bean.ResponseBeanTypeValidator;
import core.framework.util.Types;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class WebServiceInterfaceValidatorTest {
    private WebServiceInterfaceValidator validator;

    @BeforeEach
    void createWebServiceInterfaceValidator() {
        var classNameValidator = new BeanClassNameValidator();
        validator = new WebServiceInterfaceValidator(TestWebService.class, new RequestBeanMapper(classNameValidator), new ResponseBeanTypeValidator(classNameValidator));
    }

    @Test
    void validate() {
        validator.validate();
    }

    @Test
    void validateRequestBeanType() throws NoSuchMethodException {
        Method method = TestWebService.class.getDeclaredMethod("get", Integer.class);

        assertThatThrownBy(() -> validator.validateRequestBeanType(Integer.class, method))
                .isInstanceOf(Error.class).hasMessageContaining("if it is path param, please add @PathParam");

        assertThatThrownBy(() -> validator.validateRequestBeanType(TestEnum.class, method))
                .isInstanceOf(Error.class).hasMessageContaining("if it is path param, please add @PathParam");

        assertThatThrownBy(() -> validator.validateRequestBeanType(Types.map(String.class, String.class), method))
                .isInstanceOf(Error.class).hasMessageContaining("request bean type must be bean class or List<T>");
    }

    @Test
    void validateResponseBeanType() throws NoSuchMethodException {
        Method method = TestWebService.class.getDeclaredMethod("get", Integer.class);

        assertThatThrownBy(() -> validator.validateResponseBeanType(Integer.class, method))
                .isInstanceOf(Error.class).hasMessageContaining("response bean type must be bean class, Optional<T> or List<T>");

        assertThatThrownBy(() -> validator.validateResponseBeanType(Types.map(String.class, String.class), method))
                .isInstanceOf(Error.class).hasMessageContaining("response bean type must be bean class, Optional<T> or List<T>");
    }

    enum TestEnum {
        A
    }
}
