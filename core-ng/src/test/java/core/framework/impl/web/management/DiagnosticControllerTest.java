package core.framework.impl.web.management;

import core.framework.http.ContentType;
import core.framework.web.Request;
import core.framework.web.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
class DiagnosticControllerTest {
    private DiagnosticController controller;
    private Request request;

    @BeforeEach
    void createDiagnosticController() {
        controller = new DiagnosticController();
        request = Mockito.mock(Request.class);
        when(request.clientIP()).thenReturn("127.0.0.1");
    }

    @Test
    void vm() {
        Response response = controller.vm(request);
        assertThat(response.contentType()).get().isEqualTo(ContentType.TEXT_PLAIN);
    }

    @Test
    void heap() {
        Response response = controller.heap(request);
        assertThat(response.contentType()).get().isEqualTo(ContentType.TEXT_PLAIN);
    }

    @Test
    void thread() {
        Response response = controller.thread(request);
        assertThat(response.contentType()).get().isEqualTo(ContentType.TEXT_PLAIN);
    }
}
