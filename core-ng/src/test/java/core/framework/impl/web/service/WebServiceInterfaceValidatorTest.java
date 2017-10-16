package core.framework.impl.web.service;

import core.framework.impl.web.bean.RequestBeanMapper;
import core.framework.impl.web.bean.ResponseBeanTypeValidator;
import org.junit.jupiter.api.Test;

/**
 * @author neo
 */
class WebServiceInterfaceValidatorTest {
    @Test
    void validate() {
        new WebServiceInterfaceValidator(TestWebService.class, new RequestBeanMapper(), new ResponseBeanTypeValidator()).validate();
    }
}
