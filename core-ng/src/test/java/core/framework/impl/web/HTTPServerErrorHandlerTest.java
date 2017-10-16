package core.framework.impl.web;

import core.framework.api.http.HTTPStatus;
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
    private HTTPServerErrorHandler httpServerErrorHandler;

    @BeforeEach
    void createHTTPServerErrorHandler() {
        httpServerErrorHandler = new HTTPServerErrorHandler(null);
    }

    @Test
    void httpStatus() {
        assertEquals(HTTPStatus.INTERNAL_SERVER_ERROR, httpServerErrorHandler.httpStatus(new RemoteServiceException("error", Severity.WARN, "error_code")));
        assertEquals(HTTPStatus.NOT_FOUND, httpServerErrorHandler.httpStatus(new NotFoundException("error")));

        assertEquals(HTTPStatus.INTERNAL_SERVER_ERROR, httpServerErrorHandler.httpStatus(new Error()));
    }
}
