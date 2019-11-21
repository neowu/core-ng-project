package core.framework.test.web;

import core.framework.inject.Inject;
import core.framework.test.IntegrationTest;
import core.framework.web.WebContext;
import core.framework.web.service.WebServiceClientProxy;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

/**
 * @author neo
 */
class WebServiceIntegrationTest extends IntegrationTest {
    @Inject
    TestWebService service;
    @Inject
    WebContext webContext;

    @Test
    void put() {
        assertThat(service).isInstanceOf(WebServiceClientProxy.class);
        service.put(1);

        verify(service).put(1);
    }

    @Test
    void webContext() {  // check web context is registered
        assertThat(webContext).isNotNull();
    }
}
