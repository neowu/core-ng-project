package core.framework.internal.web.service;

import core.framework.api.http.HTTPStatus;
import core.framework.http.ContentType;
import core.framework.http.HTTPClient;
import core.framework.http.HTTPMethod;
import core.framework.http.HTTPRequest;
import core.framework.http.HTTPResponse;
import core.framework.internal.http.HTTPClientImpl;
import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;
import core.framework.internal.log.Trace;
import core.framework.internal.web.HTTPHandler;
import core.framework.internal.web.bean.RequestBeanWriter;
import core.framework.internal.web.bean.ResponseBeanReader;
import core.framework.log.Severity;
import core.framework.util.Maps;
import core.framework.web.service.RemoteServiceException;
import core.framework.web.service.WebServiceClientInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * @author neo
 */
public class WebServiceClient {
    public static final String USER_AGENT = "APIClient";
    private static final Logger LOGGER = LoggerFactory.getLogger(WebServiceClient.class);
    private static final Map<Integer, HTTPStatus> HTTP_STATUSES;

    static {
        HTTPStatus[] values = HTTPStatus.values();
        HTTP_STATUSES = Maps.newHashMapWithExpectedSize(values.length);
        for (HTTPStatus status : values) {
            HTTP_STATUSES.put(status.code, status);
        }
    }

    static HTTPStatus parseHTTPStatus(int statusCode) {
        HTTPStatus status = HTTP_STATUSES.get(statusCode);
        if (status == null) throw new Error("unsupported http status code, code=" + statusCode);
        return status;
    }

    private final String serviceURL;
    private final HTTPClient httpClient;
    private final RequestBeanWriter writer;
    private final ResponseBeanReader reader;
    private WebServiceClientInterceptor interceptor;

    public WebServiceClient(String serviceURL, HTTPClient httpClient, RequestBeanWriter writer, ResponseBeanReader reader) {
        this.serviceURL = serviceURL;
        this.httpClient = httpClient;
        this.writer = writer;
        this.reader = reader;
    }

    // used by generated code, must be public
    public <T> Object execute(HTTPMethod method, String path, Class<T> requestBeanClass, T requestBean, Type responseType) {
        var request = new HTTPRequest(method, serviceURL + path);
        request.accept(ContentType.APPLICATION_JSON);
        linkContext(request);

        if (requestBeanClass != null) {
            putRequestBean(request, requestBeanClass, requestBean);
        }

        if (interceptor != null) {
            LOGGER.debug("interceptor={}", interceptor.getClass().getCanonicalName());
            interceptor.onRequest(request);
        }
        HTTPResponse response = httpClient.execute(request);
        if (interceptor != null) {
            interceptor.onResponse(response);
        }

        validateResponse(response);
        try {
            return reader.fromJSON(responseType, response.body);
        } catch (IOException e) {
            // for security concern, to hide original error message, jackson may return detailed info, e.g. possible allowed values for enum
            // detailed info can still be found in trace log or exception stack trace
            throw new UncheckedIOException("failed to deserialize remote service response, responseType=" + responseType.getTypeName(), e);
        }
    }

    // used by generated code, must be public
    public void intercept(WebServiceClientInterceptor interceptor) {
        if (this.interceptor != null) throw new Error("found duplicate interceptor, previous=" + this.interceptor.getClass().getCanonicalName());
        this.interceptor = interceptor;
    }

    // used by generated code, must be public
    public void logCallWebService(String method) {
        LOGGER.debug("call web service, method={}", method);
    }

    <T> void putRequestBean(HTTPRequest request, Class<T> requestBeanClass, T requestBean) {
        HTTPMethod method = request.method;
        if (method == HTTPMethod.GET || method == HTTPMethod.DELETE) {
            Map<String, String> queryParams = writer.toParams(requestBeanClass, requestBean);
            request.params.putAll(queryParams);
        } else if (method == HTTPMethod.POST || method == HTTPMethod.PUT || method == HTTPMethod.PATCH) {
            byte[] json = writer.toJSON(requestBeanClass, requestBean);
            request.body(json, ContentType.APPLICATION_JSON);
        } else {
            throw new Error("not supported method, method=" + method);
        }
    }

    void linkContext(HTTPRequest request) {
        Map<String, String> headers = request.headers;
        headers.put(HTTPHandler.HEADER_CLIENT.toString(), LogManager.APP_NAME);

        ActionLog actionLog = LogManager.CURRENT_ACTION_LOG.get();
        if (actionLog == null) return;  // web service client may be used without action log context

        headers.put(HTTPHandler.HEADER_CORRELATION_ID.toString(), actionLog.correlationId());
        if (actionLog.trace == Trace.CASCADE) headers.put(HTTPHandler.HEADER_TRACE.toString(), actionLog.trace.name());
        headers.put(HTTPHandler.HEADER_REF_ID.toString(), actionLog.id);

        long timeout = ((HTTPClientImpl) httpClient).timeoutInNano; // not count connect timeout, as action starts after connecting
        long remainingTime = actionLog.remainingProcessTimeInNano();
        if (remainingTime > 0 && remainingTime < timeout) timeout = remainingTime;  // if remaining time is 0, means current action already triggered LONG_PROCESS, no need to propagate to child actions, to reduce unnecessary trace
        headers.put(HTTPHandler.HEADER_TIMEOUT.toString(), String.valueOf(timeout));
    }

    void validateResponse(HTTPResponse response) {
        int statusCode = response.statusCode;
        if (statusCode >= 200 && statusCode < 300) return;

        // handle empty body gracefully, e.g. 503 during deployment
        // handle html error message gracefully, e.g. public cloud LB failed to connect to backend
        if (response.body.length > 0 && response.contentType != null && ContentType.APPLICATION_JSON.mediaType.equals(response.contentType.mediaType)) {
            InternalErrorResponse error = errorResponse(response);
            if (error.id != null && error.errorCode != null) {  // use manual validation rather than annotation to keep the flow straightforward and less try/catch, check if valid error response json
                LOGGER.debug("failed to call remote service, statusCode={}, id={}, severity={}, errorCode={}, remoteStackTrace={}", statusCode, error.id, error.severity, error.errorCode, error.stackTrace);
                throw new RemoteServiceException(error.message, parseSeverity(error.severity), error.errorCode, parseHTTPStatus(statusCode));
            }
        }
        throw new RemoteServiceException("failed to call remote service, statusCode=" + statusCode, Severity.ERROR, "REMOTE_SERVICE_ERROR", parseHTTPStatus(statusCode));
    }

    private InternalErrorResponse errorResponse(HTTPResponse response) {
        try {
            return (InternalErrorResponse) reader.fromJSON(InternalErrorResponse.class, response.body);
        } catch (Throwable e) {
            int statusCode = response.statusCode;
            throw new RemoteServiceException("failed to deserialize remote service error response, statusCode=" + statusCode, Severity.ERROR, "REMOTE_SERVICE_ERROR", parseHTTPStatus(statusCode), e);
        }
    }

    private Severity parseSeverity(String severity) {
        if (severity == null) return Severity.ERROR;
        return Severity.valueOf(severity);
    }
}
