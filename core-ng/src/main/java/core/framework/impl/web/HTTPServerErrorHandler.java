package core.framework.impl.web;

import core.framework.api.http.HTTPStatus;
import core.framework.api.web.service.ResponseStatus;
import core.framework.http.ContentType;
import core.framework.impl.web.request.RequestImpl;
import core.framework.impl.web.response.ResponseHandler;
import core.framework.impl.web.response.ResponseImpl;
import core.framework.impl.web.service.ErrorResponse;
import core.framework.log.ActionLogContext;
import core.framework.log.ErrorCode;
import core.framework.log.Severity;
import core.framework.util.Exceptions;
import core.framework.web.ErrorHandler;
import core.framework.web.Response;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * @author neo
 */
public class HTTPServerErrorHandler {
    private final Logger logger = LoggerFactory.getLogger(HTTPServerErrorHandler.class);
    private final ResponseHandler responseHandler;
    public ErrorHandler customErrorHandler;

    HTTPServerErrorHandler(ResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
    }

    void handleError(Throwable e, HttpServerExchange exchange, RequestImpl request) {
        if (exchange.isResponseStarted()) {
            logger.error("response was sent, discard the current http transaction");
            return;
        }

        try {
            Response errorResponse = null;
            if (customErrorHandler != null) {
                Optional<Response> customErrorResponse = customErrorHandler.handle(request, e);
                if (customErrorResponse.isPresent()) errorResponse = customErrorResponse.get();
            }
            if (errorResponse == null) {
                String accept = exchange.getRequestHeaders().getFirst(Headers.ACCEPT);
                errorResponse = defaultErrorResponse(e, accept);
            }
            responseHandler.render((ResponseImpl) errorResponse, exchange);
        } catch (Throwable error) {
            logger.error(error.getMessage(), e);
            if (exchange.isResponseStarted()) {
                logger.error("failed to render error page, response was sent, discard the current http transaction");
                return;
            }
            renderDefaultErrorPage(error, exchange);
        }
    }

    private Response defaultErrorResponse(Throwable e, String accept) {
        HTTPStatus status = httpStatus(e);

        if (accept != null && accept.contains(ContentType.APPLICATION_JSON.mediaType())) {
            return Response.bean(errorResponse(e)).status(status);
        } else {
            return Response.text(errorHTML(e)).status(status).contentType(ContentType.TEXT_HTML);
        }
    }

    HTTPStatus httpStatus(Throwable e) {
        ResponseStatus responseStatus = e.getClass().getDeclaredAnnotation(ResponseStatus.class);
        if (responseStatus != null) return responseStatus.value();

        return HTTPStatus.INTERNAL_SERVER_ERROR;
    }

    private String errorHTML(Throwable e) {
        return "<html><body><h1>Error</h1><p>" + e.getMessage() + "</p><pre>" + Exceptions.stackTrace(e) + "</pre></body></html>";
    }

    private void renderDefaultErrorPage(Throwable e, HttpServerExchange exchange) {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, ContentType.TEXT_HTML.toString());
        exchange.setStatusCode(HTTPStatus.INTERNAL_SERVER_ERROR.code);
        ActionLogContext.put("responseCode", exchange.getStatusCode());
        exchange.getResponseSender().send(errorHTML(e));
    }

    ErrorResponse errorResponse(Throwable e) {
        ErrorResponse response = new ErrorResponse();
        response.id = ActionLogContext.id();
        response.message = e.getMessage();
        response.stackTrace = Exceptions.stackTrace(e);
        if (e instanceof ErrorCode) {
            ErrorCode errorCode = (ErrorCode) e;
            response.errorCode = errorCode.errorCode();
            response.severity = errorCode.severity().name();
        } else {
            response.errorCode = "INTERNAL_ERROR";
            response.severity = Severity.ERROR.name();
        }
        return response;
    }
}
