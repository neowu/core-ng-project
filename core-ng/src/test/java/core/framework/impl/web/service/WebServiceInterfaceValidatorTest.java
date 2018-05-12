package core.framework.impl.web.service;

import core.framework.impl.web.bean.RequestBeanMapper;
import core.framework.impl.web.bean.ResponseBeanTypeValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    void validateRequestBeanType() {
        assertThatThrownBy(() -> validator.validateRequestBeanType(Integer.class, TestWebService.class.getDeclaredMethod("get", Integer.class)))
                .isInstanceOf(Error.class).hasMessageContaining("if it is path param, please add @PathParam");
    }
}
