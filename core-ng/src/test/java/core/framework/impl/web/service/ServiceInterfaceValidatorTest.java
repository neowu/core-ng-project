package core.framework.impl.web.service;

import core.framework.impl.web.BeanValidator;
import org.junit.Test;

/**
 * @author neo
 */
public class ServiceInterfaceValidatorTest {
    @Test
    public void validate() {
        new ServiceInterfaceValidator(TestWebService.class, new BeanValidator()).validate();
    }
}