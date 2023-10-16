package core.framework.internal.web.sys;

import core.framework.http.ContentType;
import core.framework.web.Request;
import core.framework.web.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
@ExtendWith(MockitoExtension.class)
class DiagnosticControllerTest {
    @Mock
    Request request;
    private DiagnosticController controller;

    @BeforeEach
    void createDiagnosticController() {
        when(request.clientIP()).thenReturn("127.0.0.1");
        controller = new DiagnosticController();
    }

    @Test
    void vm() {
        Response response = controller.vm(request);
        assertThat(response.contentType()).hasValue(ContentType.TEXT_PLAIN);
    }

    @Test
    void heap() {
        Response response = controller.heap(request);
        assertThat(response.contentType()).hasValue(ContentType.TEXT_PLAIN);
    }

    @Test
    void thread() {
        Response response = controller.thread(request);
        assertThat(response.contentType()).hasValue(ContentType.TEXT_PLAIN);
    }

    @Test
    void virtualThread() {
        Response response = controller.virtualThread(request);
        assertThat(response.contentType()).hasValue(ContentType.TEXT_PLAIN);
    }

    @Test
    void proc() {
        Response response = controller.proc(request);
        assertThat(response.contentType()).hasValue(ContentType.TEXT_PLAIN);
    }
}
