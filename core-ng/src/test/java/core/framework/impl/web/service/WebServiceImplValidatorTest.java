package core.framework.impl.web.service;

import org.junit.Test;

/**
 * @author neo
 */
public class WebServiceImplValidatorTest {
    @Test
    public void validate() {
        new WebServiceImplValidator<>(TestWebService.class, new TestWebServiceImpl()).validate();
    }
}