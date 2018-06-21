package core.framework.impl.web;

import core.framework.api.http.HTTPStatus;
import core.framework.impl.web.service.ErrorResponse;
import core.framework.log.Severity;
import core.framework.web.exception.NotFoundException;
import core.framework.web.service.RemoteServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class HTTPServerErrorHandlerTest {
    private HTTPServerErrorHandler handler;

    @BeforeEach
    void createHTTPServerErrorHandler() {
        handler = new HTTPServerErrorHandler(null);
    }

    @Test
    void httpStatus() {
        assertThat(handler.httpStatus(new RemoteServiceException("error", Severity.WARN, "error_code", HTTPStatus.BAD_REQUEST)))
                .isEqualTo(HTTPStatus.BAD_REQUEST);

        assertThat(handler.httpStatus(new NotFoundException("error"))).isEqualTo(HTTPStatus.NOT_FOUND);

        assertThat(handler.httpStatus(new Error())).isEqualTo(HTTPStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void errorResponseWithErrorCodeException() {
        ErrorResponse response = handler.errorResponse(new NotFoundException("test-message", "TEST_ERROR_CODE"), null);

        assertThat(response.message).isEqualTo("test-message");
        assertThat(response.errorCode).isEqualTo("TEST_ERROR_CODE");
        assertThat(response.severity).isEqualTo("WARN");
    }

    @Test
    void errorResponse() {
        ErrorResponse response = handler.errorResponse(new Error("test-message"), null);

        assertThat(response.message).isEqualTo("test-message");
        assertThat(response.errorCode).isEqualTo("INTERNAL_ERROR");
        assertThat(response.severity).isEqualTo("ERROR");
    }
}
