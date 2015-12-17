package core.framework.impl.web;

import core.framework.api.http.ContentType;
import core.framework.api.http.HTTPStatus;
import core.framework.api.log.ActionLogContext;
import core.framework.api.log.ErrorCode;
import core.framework.api.util.Exceptions;
import core.framework.api.validate.ValidationException;
import core.framework.api.web.ErrorHandler;
import core.framework.api.web.Response;
import core.framework.api.web.ResponseImpl;
import core.framework.api.web.exception.BadRequestException;
import core.framework.api.web.exception.ConflictException;
import core.framework.api.web.exception.ForbiddenException;
import core.framework.api.web.exception.MethodNotAllowedException;
import core.framework.api.web.exception.NotFoundException;
import core.framework.api.web.exception.RemoteServiceException;
import core.framework.api.web.exception.UnauthorizedException;
import core.framework.impl.web.exception.ErrorResponse;
import core.framework.impl.web.request.RequestImpl;
import core.framework.impl.web.response.ResponseHandler;
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

    public HTTPServerErrorHandler(ResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
    }

    public void handleError(Throwable e, HttpServerExchange exchange, RequestImpl request) {
        if (exchange.isResponseStarted()) {
            logger.error("response was sent, discard the current http transaction");
            return;
        }

        renderErrorPage(e, exchange, request);
    }

    private void renderErrorPage(Throwable e, HttpServerExchange exchange, RequestImpl request) {
        try {
            Response errorResponse = null;
            if (customErrorHandler != null) {
                Optional<Response> customErrorResponse = customErrorHandler.handle(request, e);
                if (customErrorResponse.isPresent()) errorResponse = customErrorResponse.get();
            }
            String accept = exchange.getRequestHeaders().getFirst(Headers.ACCEPT);
            if (errorResponse == null) errorResponse = defaultErrorResponse(e, accept);
            responseHandler.handle((ResponseImpl) errorResponse, exchange, request);
        } catch (Throwable error) {
            if (exchange.isResponseStarted()) {
                logger.error("response was sent, discard the current http transaction", error);
                return;
            }
            renderDefaultErrorPage(error, exchange);
        }
    }

    private Response defaultErrorResponse(Throwable e, String accept) {
        HTTPStatus status;

        if (e instanceof BadRequestException || e instanceof ValidationException) {
            status = HTTPStatus.BAD_REQUEST;
        } else if (e instanceof MethodNotAllowedException) {
            status = HTTPStatus.METHOD_NOT_ALLOWED;
        } else if (e instanceof NotFoundException) {
            status = HTTPStatus.NOT_FOUND;
        } else if (e instanceof UnauthorizedException) {
            status = HTTPStatus.UNAUTHORIZED;
        } else if (e instanceof ForbiddenException) {
            status = HTTPStatus.FORBIDDEN;
        } else if (e instanceof ConflictException) {
            status = HTTPStatus.CONFLICT;
        } else {
            status = HTTPStatus.INTERNAL_SERVER_ERROR;
        }

        if (accept != null && accept.contains(ContentType.APPLICATION_JSON.mediaType())) {
            return Response.bean(errorResponse(e), status);
        } else {
            return Response.text(errorHTML(e), status, ContentType.TEXT_HTML);
        }
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

    private ErrorResponse errorResponse(Throwable e) {
        ErrorResponse response = new ErrorResponse();
        response.id = ActionLogContext.id();
        response.message = e.getMessage();
        response.stackTrace = Exceptions.stackTrace(e);
        if (e instanceof ErrorCode) response.errorCode = ((ErrorCode) e).errorCode();
        else if (e instanceof ValidationException) response.errorCode = "VALIDATION_ERROR";
        else if (e instanceof RemoteServiceException) response.errorCode = ((RemoteServiceException) e).errorCode;
        else response.errorCode = "INTERNAL_ERROR";
        return response;
    }
}
