package core.framework.impl.web.service;

import core.framework.api.http.HTTPStatus;
import core.framework.http.ContentType;
import core.framework.http.HTTPClient;
import core.framework.http.HTTPMethod;
import core.framework.http.HTTPRequest;
import core.framework.http.HTTPResponse;
import core.framework.impl.log.ActionLog;
import core.framework.impl.log.LogManager;
import core.framework.impl.web.HTTPHandler;
import core.framework.impl.web.bean.RequestBeanMapper;
import core.framework.impl.web.bean.ResponseBeanMapper;
import core.framework.log.Severity;
import core.framework.util.Maps;
import core.framework.web.service.RemoteServiceException;
import core.framework.web.service.WebServiceClientInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.Map;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public class WebServiceClient {
    public static final String USER_AGENT = "APIClient";
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

    private final Logger logger = LoggerFactory.getLogger(WebServiceClient.class);
    private final String serviceURL;
    private final HTTPClient httpClient;
    private final RequestBeanMapper requestBeanMapper;
    private final ResponseBeanMapper responseBeanMapper;
    private WebServiceClientInterceptor interceptor;

    public WebServiceClient(String serviceURL, HTTPClient httpClient, RequestBeanMapper requestBeanMapper, ResponseBeanMapper responseBeanMapper) {
        this.serviceURL = serviceURL;
        this.httpClient = httpClient;
        this.requestBeanMapper = requestBeanMapper;
        this.responseBeanMapper = responseBeanMapper;
    }

    // used by generated code, must be public
    public <T> Object execute(HTTPMethod method, String path, Class<T> requestBeanClass, T requestBean, Type responseType) {
        var request = new HTTPRequest(method, serviceURL + path);
        request.accept(ContentType.APPLICATION_JSON);
        linkContext(request);

        if (requestBeanClass != null) {
            putRequestBean(request, method, requestBeanClass, requestBean);
        }

        if (interceptor != null) {
            logger.debug("intercept request, interceptor={}", interceptor.getClass().getCanonicalName());
            interceptor.intercept(request);
        }

        HTTPResponse response = httpClient.execute(request);
        validateResponse(response);
        return responseBeanMapper.fromJSON(responseType, response.body);
    }

    public void intercept(WebServiceClientInterceptor interceptor) {
        if (this.interceptor != null) throw new Error(format("found duplicate interceptor, previous={}", this.interceptor.getClass().getCanonicalName()));
        this.interceptor = interceptor;
    }

    <T> void putRequestBean(HTTPRequest request, HTTPMethod method, Class<T> requestBeanClass, T requestBean) {
        if (method == HTTPMethod.GET || method == HTTPMethod.DELETE) {
            Map<String, String> queryParams = requestBeanMapper.toParams(requestBeanClass, requestBean);
            request.params.putAll(queryParams);
        } else if (method == HTTPMethod.POST || method == HTTPMethod.PUT || method == HTTPMethod.PATCH) {
            byte[] json = requestBeanMapper.toJSON(requestBeanClass, requestBean);
            request.body(json, ContentType.APPLICATION_JSON);
        } else {
            throw new Error("not supported method, method=" + method);
        }
    }

    private void linkContext(HTTPRequest request) {
        Map<String, String> headers = request.headers;
        headers.put(HTTPHandler.HEADER_CLIENT.toString(), LogManager.APP_NAME);

        ActionLog actionLog = LogManager.CURRENT_ACTION_LOG.get();
        if (actionLog == null) return;  // web service client may be used without action log context

        headers.put(HTTPHandler.HEADER_CORRELATION_ID.toString(), actionLog.correlationId());
        if (actionLog.trace) headers.put(HTTPHandler.HEADER_TRACE.toString(), "true");
        headers.put(HTTPHandler.HEADER_REF_ID.toString(), actionLog.id);
    }

    void validateResponse(HTTPResponse response) {
        int statusCode = response.statusCode;
        if (statusCode >= 200 && statusCode < 300) return;
        HTTPStatus status = parseHTTPStatus(statusCode);
        // handle empty body, e.g. 503 during deployment
        if (response.body.length == 0) throw new RemoteServiceException(format("failed to call remote service, statusCode={}", statusCode), Severity.ERROR, "REMOTE_SERVICE_ERROR", status);
        try {
            ErrorResponse error = (ErrorResponse) responseBeanMapper.fromJSON(ErrorResponse.class, response.body);
            logger.debug("failed to call remote service, statusCode={}, id={}, severity={}, errorCode={}, remoteStackTrace={}", statusCode, error.id, error.severity, error.errorCode, error.stackTrace);
            throw new RemoteServiceException(format("failed to call remote service, statusCode={}, error={}", statusCode, error.message), parseSeverity(error.severity), error.errorCode, status);
        } catch (RemoteServiceException e) {
            throw e;
        } catch (Throwable e) {
            throw new RemoteServiceException(format("internal communication failed, statusCode={}, responseText={}", statusCode, response.text()), Severity.ERROR, "REMOTE_SERVICE_ERROR", status, e);
        }
    }

    private Severity parseSeverity(String severity) {
        if (severity == null) return Severity.ERROR;
        return Severity.valueOf(severity);
    }
}
