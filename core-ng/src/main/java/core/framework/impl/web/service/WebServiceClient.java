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
        HTTPStatus status = response.status;
        if (status.code >= 200 && status.code < 300) return;
        try {
            ErrorResponse error = (ErrorResponse) responseBeanMapper.fromJSON(ErrorResponse.class, response.body);
            logger.debug("failed to call remote service, id={}, severity={}, errorCode={}, remoteStackTrace={}", error.id, error.severity, error.errorCode, error.stackTrace);
            throw new RemoteServiceException(error.message, parseSeverity(error.severity), error.errorCode, status);
        } catch (RemoteServiceException e) {
            throw e;
        } catch (Throwable e) {
            String responseText = response.text();
            logger.warn("failed to decode response, statusCode={}, responseText={}", status.code, responseText, e);
            throw new RemoteServiceException(format("internal communication failed, status={}, responseText={}", status.code, responseText), Severity.ERROR, "REMOTE_SERVICE_ERROR", status, e);
        }
    }

    private Severity parseSeverity(String severity) {
        if (severity == null) return Severity.ERROR;
        return Severity.valueOf(severity);
    }
}
