package core.framework.impl.web.service;

import core.framework.impl.web.bean.BeanValidator;
import core.framework.impl.web.bean.RequestBeanMapper;
import org.junit.Test;

/**
 * @author neo
 */
public class WebServiceInterfaceValidatorTest {
    @Test
    public void validate() {
        new WebServiceInterfaceValidator(TestWebService.class, new BeanValidator(), new RequestBeanMapper()).validate();
    }
}
