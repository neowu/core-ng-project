package core.framework.impl.web;

import core.framework.api.http.HTTPStatus;
import core.framework.impl.web.service.ErrorResponse;
import core.framework.log.Severity;
import core.framework.web.exception.NotFoundException;
import core.framework.web.service.RemoteServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        assertEquals(HTTPStatus.INTERNAL_SERVER_ERROR, handler.httpStatus(new RemoteServiceException("error", Severity.WARN, "error_code")));
        assertEquals(HTTPStatus.NOT_FOUND, handler.httpStatus(new NotFoundException("error")));

        assertEquals(HTTPStatus.INTERNAL_SERVER_ERROR, handler.httpStatus(new Error()));
    }

    @Test
    void errorResponseWithErrorCodeException() {
        ErrorResponse response = handler.errorResponse(new NotFoundException("test-message", "TEST_ERROR_CODE"));

        assertEquals("test-message", response.message);
        assertEquals("TEST_ERROR_CODE", response.errorCode);
        assertEquals("WARN", response.severity);
    }

    @Test
    void errorResponse() {
        ErrorResponse response = handler.errorResponse(new Error("test-message"));

        assertEquals("test-message", response.message);
        assertEquals("INTERNAL_ERROR", response.errorCode);
        assertEquals("ERROR", response.severity);
    }
}
