package core.framework.impl.web.service;

import org.junit.jupiter.api.Test;

/**
 * @author neo
 */
class WebServiceImplValidatorTest {
    @Test
    void validate() {
        new WebServiceImplValidator<>(TestWebService.class, new WebServiceControllerBuilderTest.TestWebServiceImpl()).validate();
    }
}
