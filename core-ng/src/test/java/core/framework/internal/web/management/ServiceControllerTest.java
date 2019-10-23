package core.framework.internal.web.management;

import core.framework.api.http.HTTPStatus;
import core.framework.internal.module.ServiceRegistry;
import core.framework.internal.web.service.TestWebService;
import core.framework.web.Request;
import core.framework.web.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
class ServiceControllerTest {
    private ServiceController controller;
    private ServiceRegistry registry;

    @BeforeEach
    void createServiceController() {
        registry = new ServiceRegistry();
        controller = new ServiceController(registry);
    }

    @Test
    void execute() {
        Request request = mock(Request.class);
        when(request.clientIP()).thenReturn("127.0.0.1");
        Response response = controller.execute(request);

        assertThat(response.status()).isEqualTo(HTTPStatus.OK);
    }

    @Test
    void serviceResponse() {
        registry.serviceInterfaces.add(TestWebService.class);

        ServiceResponse response = controller.serviceResponse();

        assertThat(response.services).contains("TestWebService");
    }
}
