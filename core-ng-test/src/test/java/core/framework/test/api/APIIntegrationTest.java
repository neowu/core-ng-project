package core.framework.test.api;

import core.framework.impl.web.service.WebServiceClientProxy;
import core.framework.inject.Inject;
import core.framework.test.IntegrationTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

/**
 * @author neo
 */
class APIIntegrationTest extends IntegrationTest {
    @Inject
    TestWebService service;

    @Test
    void put() {
        assertThat(service).isInstanceOf(WebServiceClientProxy.class);
        service.put(1);

        verify(service).put(1);
    }
}
