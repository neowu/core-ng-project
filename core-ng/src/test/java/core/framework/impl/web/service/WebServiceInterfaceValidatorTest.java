package core.framework.impl.web.service;

import core.framework.impl.web.bean.RequestBeanMapper;
import core.framework.impl.web.bean.ResponseBeanTypeValidator;
import org.junit.Test;

/**
 * @author neo
 */
public class WebServiceInterfaceValidatorTest {
    @Test
    public void validate() {
        new WebServiceInterfaceValidator(TestWebService.class, new RequestBeanMapper(), new ResponseBeanTypeValidator()).validate();
    }
}
