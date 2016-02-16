package core.framework.impl.web.service;

import core.framework.api.http.ContentType;
import core.framework.api.http.HTTPClient;
import core.framework.api.http.HTTPHeaders;
import core.framework.api.http.HTTPMethod;
import core.framework.api.http.HTTPRequest;
import core.framework.api.http.HTTPResponse;
import core.framework.api.http.HTTPStatus;
import core.framework.api.util.Encodings;
import core.framework.api.util.Exceptions;
import core.framework.api.util.Maps;
import core.framework.api.util.Strings;
import core.framework.api.validate.ValidationException;
import core.framework.api.web.exception.RemoteServiceException;
import core.framework.api.web.service.WebServiceRequestSigner;
import core.framework.impl.json.JSONMapper;
import core.framework.impl.log.ActionLog;
import core.framework.impl.log.LogManager;
import core.framework.impl.web.BeanValidator;
import core.framework.impl.web.HTTPServerHandler;
import core.framework.impl.web.exception.ErrorResponse;
import core.framework.impl.web.route.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * @author neo
 */
public class WebServiceClient {
    private final Logger logger = LoggerFactory.getLogger(WebServiceClient.class);

    private final String serviceURL;
    private final HTTPClient httpClient;
    private final BeanValidator validator;
    private final LogManager logManager;
    public WebServiceRequestSigner signer;

    public WebServiceClient(String serviceURL, HTTPClient httpClient, BeanValidator validator, LogManager logManager) {
        this.serviceURL = serviceURL;
        this.httpClient = httpClient;
        this.validator = validator;
        this.logManager = logManager;
    }

    public String serviceURL(String pathPattern, Map<String, Object> pathParams) {
        StringBuilder builder = new StringBuilder(serviceURL);
        Path path = Path.parse(pathPattern).next; // skip the first '/'
        while (path != null) {
            String value = path.value;
            if ("/".equals(value)) {
                builder.append(value);
            } else if (value.startsWith(":")) {
                int paramIndex = value.indexOf('(');
                int endIndex = paramIndex > 0 ? paramIndex : value.length();
                String variable = value.substring(1, endIndex);
                String pathParam = pathParam(pathParams, variable);
                builder.append('/').append(Encodings.uriComponent(pathParam));
            } else {
                builder.append('/').append(value);
            }
            path = path.next;
        }
        return builder.toString();
    }

    private String pathParam(Map<String, Object> pathParams, String variable) {
        Object param = pathParams.get(variable);
        if (param == null) throw new ValidationException(Maps.newHashMap(variable, Strings.format("path param must not null, name={}", variable)));
        // convert logic matches PathParams
        if (param instanceof String) {
            String paramValue = (String) param;
            if (Strings.isEmpty(paramValue))
                throw new ValidationException(Maps.newHashMap(variable, Strings.format("path param must not be empty, name={}", variable)));
            return paramValue;
        } else if (param instanceof Integer) {
            return String.valueOf(param);
        } else {
            return JSONMapper.toJSONValue(param);
        }
    }

    public Object execute(HTTPMethod method, String serviceURL, Type requestType, Object requestBean, Type responseType) {
        if (requestType != null) {
            validator.validate(requestType, requestBean);
        }

        HTTPRequest request = new HTTPRequest(method, serviceURL);
        request.header(HTTPHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString());

        if (logManager.appName != null) {
            request.header(HTTPServerHandler.HEADER_CLIENT, logManager.appName);
        }

        linkContext(request);

        if (requestBean != null) {
            if (method == HTTPMethod.GET || method == HTTPMethod.DELETE) {
                Map<String, String> queryParams = JSONMapper.toMapValue(requestBean);
                addQueryParams(request, queryParams);
            } else if (method == HTTPMethod.POST || method == HTTPMethod.PUT) {
                byte[] json = JSONMapper.toJSON(requestBean);
                request.body(json, ContentType.APPLICATION_JSON);
            } else {
                throw Exceptions.error("not supported method, method={}", method);
            }
        }

        if (signer != null) {
            logger.debug("sign request, signer={}", signer.getClass().getCanonicalName());
            signer.sign(request);
        }

        HTTPResponse response = httpClient.execute(request);
        validateResponse(response);

        if (void.class != responseType) {
            return JSONMapper.fromJSON(responseType, response.body());
        } else {
            return null;
        }
    }

    void addQueryParams(HTTPRequest request, Map<String, String> queryParams) {
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            String value = entry.getValue();
            if (value != null) request.addParam(entry.getKey(), value);
        }
    }

    private void linkContext(HTTPRequest httpRequest) {
        ActionLog actionLog = logManager.currentActionLog();
        if (actionLog == null) return;  // web service client may be used without action log context

        httpRequest.header(HTTPServerHandler.HEADER_REF_ID, actionLog.refId());

        if (actionLog.trace) {
            httpRequest.header(HTTPServerHandler.HEADER_TRACE, "true");
        }
    }

    private void validateResponse(HTTPResponse response) {
        HTTPStatus status = response.status();
        if (status.code >= HTTPStatus.OK.code && status.code <= 300) return;
        byte[] responseBody = response.body();
        try {
            ErrorResponse error = JSONMapper.fromJSON(ErrorResponse.class, responseBody);
            logger.debug("failed to call remote service, id={}, errorCode={}, remoteStackTrace={}", error.id, error.errorCode, error.stackTrace);
            RemoteServiceException exception = new RemoteServiceException(error.message, status, error.errorCode);
            exception.id = error.id;
            throw exception;
        } catch (RemoteServiceException e) {
            throw e;
        } catch (Exception e) {
            String responseText = response.text();
            logger.warn("failed to decode response, statusCode={}, responseText={}", status.code, responseText, e);
            throw new RemoteServiceException(Strings.format("internal communication failed, status={}, responseText={}", status.code, responseText), status, "REMOTE_ERROR", e);
        }
    }
}
