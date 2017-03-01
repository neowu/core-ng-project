package core.framework.impl.web.service;

import core.framework.impl.web.BeanValidator;
import org.junit.Test;

/**
 * @author neo
 */
public class WebServiceInterfaceValidatorTest {
    @Test
    public void validate() {
        new WebServiceInterfaceValidator(TestWebService.class, new BeanValidator()).validate();
    }
}