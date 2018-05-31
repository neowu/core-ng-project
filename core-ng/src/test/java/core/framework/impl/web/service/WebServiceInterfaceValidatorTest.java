package core.framework.impl.web.service;

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
        validator = new WebServiceInterfaceValidator(TestWebService.class, new RequestBeanMapper(), new ResponseBeanTypeValidator());
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
    }

    @Test
    void validateResponseBeanType() throws NoSuchMethodException {
        Method method = TestWebService.class.getDeclaredMethod("get", Integer.class);

        assertThatThrownBy(() -> validator.validateResponseBeanType(Integer.class, method))
                .isInstanceOf(Error.class).hasMessageContaining("response bean type must be bean class");

        assertThatThrownBy(() -> validator.validateResponseBeanType(Types.map(String.class, String.class), method))
                .isInstanceOf(Error.class).hasMessageContaining("response bean type must be bean class");
    }
}
