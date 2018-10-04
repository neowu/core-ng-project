package core.framework.impl.web;

import core.framework.api.http.HTTPStatus;
import core.framework.api.web.service.ResponseStatus;
import core.framework.http.ContentType;
import core.framework.impl.log.ActionLog;
import core.framework.impl.web.request.RequestImpl;
import core.framework.impl.web.response.ResponseHandler;
import core.framework.impl.web.response.ResponseImpl;
import core.framework.impl.web.service.ErrorResponse;
import core.framework.impl.web.service.WebServiceClient;
import core.framework.impl.web.site.AJAXErrorResponse;
import core.framework.log.ErrorCode;
import core.framework.log.Severity;
import core.framework.util.Exceptions;
import core.framework.web.ErrorHandler;
import core.framework.web.Response;
import core.framework.web.service.RemoteServiceException;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author neo
 */
public class HTTPErrorHandler {
    private final Logger logger = LoggerFactory.getLogger(HTTPErrorHandler.class);
    private final ResponseHandler responseHandler;
    public ErrorHandler customErrorHandler;

    HTTPErrorHandler(ResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
    }

    void handleError(Throwable e, HttpServerExchange exchange, RequestImpl request, ActionLog actionLog) {
        if (exchange.isResponseStarted()) {
            logger.error("response was sent, discard the current http transaction");
            return;
        }

        try {
            Response errorResponse = null;
            if (customErrorHandler != null) errorResponse = customErrorHandler.handle(request, e).orElse(null);
            if (errorResponse == null) errorResponse = defaultErrorResponse(e, exchange, actionLog);
            responseHandler.render((ResponseImpl) errorResponse, exchange, actionLog);
        } catch (Throwable error) {
            logger.error(error.getMessage(), e);
            if (exchange.isResponseStarted()) {
                logger.error("failed to render error page, response was sent, discard the current http transaction");
                return;
            }
            renderDefaultErrorPage(error, exchange, actionLog);
        }
    }

    private Response defaultErrorResponse(Throwable e, HttpServerExchange exchange, ActionLog actionLog) {
        HTTPStatus status = httpStatus(e);

        HeaderMap headers = exchange.getRequestHeaders();
        String accept = headers.getFirst(Headers.ACCEPT);

        if (accept != null && accept.contains(ContentType.APPLICATION_JSON.mediaType)) {
            String userAgent = headers.getFirst(Headers.USER_AGENT);
            return Response.bean(errorResponse(e, userAgent, actionLog.id)).status(status);
        } else {
            return Response.text(errorHTML(e, actionLog.id)).status(status).contentType(ContentType.TEXT_HTML);
        }
    }

    Object errorResponse(Throwable e, String userAgent, String actionId) {
        if (WebServiceClient.USER_AGENT.equals(userAgent)) {
            var response = new ErrorResponse();
            response.id = actionId;
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
        } else {
            var response = new AJAXErrorResponse();
            response.id = actionId;
            response.message = e.getMessage();
            if (e instanceof ErrorCode) {
                response.errorCode = ((ErrorCode) e).errorCode();
            } else {
                response.errorCode = "INTERNAL_ERROR";
            }
            return response;
        }
    }

    HTTPStatus httpStatus(Throwable e) {
        ResponseStatus responseStatus = e.getClass().getDeclaredAnnotation(ResponseStatus.class);
        if (responseStatus != null) return responseStatus.value();
        if (e instanceof RemoteServiceException) return ((RemoteServiceException) e).status;    // propagate underlying status code for REST convention
        return HTTPStatus.INTERNAL_SERVER_ERROR;
    }

    String errorHTML(Throwable e, String actionId) {
        String errorCode = e instanceof ErrorCode ? ((ErrorCode) e).errorCode() : "ERROR";
        return "<html><body><h1>" + errorCode + "</h1><p>" + e.getMessage() + "</p><p>id: " + actionId + "</p></body></html>";
    }

    private void renderDefaultErrorPage(Throwable e, HttpServerExchange exchange, ActionLog actionLog) {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, ContentType.TEXT_HTML.toString());
        exchange.setStatusCode(HTTPStatus.INTERNAL_SERVER_ERROR.code);
        actionLog.context("responseCode", exchange.getStatusCode());
        exchange.getResponseSender().send(errorHTML(e, actionLog.id));
    }
}
