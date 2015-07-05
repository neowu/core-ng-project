package core.framework.impl.web.client;

import core.framework.api.http.ContentTypes;
import core.framework.api.http.HTTPClient;
import core.framework.api.http.HTTPMethod;
import core.framework.api.http.HTTPRequest;
import core.framework.api.http.HTTPResponse;
import core.framework.api.http.HTTPStatus;
import core.framework.api.log.ActionLogContext;
import core.framework.api.module.WebServiceClientConfig;
import core.framework.api.util.Exceptions;
import core.framework.api.util.JSON;
import core.framework.api.util.Types;
import core.framework.api.web.client.WebServiceRequestSigner;
import core.framework.api.web.exception.RemoteServiceException;
import core.framework.api.web.exception.ValidationException;
import core.framework.impl.web.BeanValidator;
import core.framework.impl.web.HTTPServerHandler;
import core.framework.impl.web.exception.ErrorResponse;
import core.framework.impl.web.exception.ValidationErrorResponse;
import core.framework.impl.web.route.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * @author neo
 */
public class WebServiceClient implements WebServiceClientConfig {
    private final Logger logger = LoggerFactory.getLogger(WebServiceClient.class);

    private final String serviceURL;
    private final HTTPClient httpClient;
    final BeanValidator validator;
    private WebServiceRequestSigner signer;

    public WebServiceClient(String serviceURL, HTTPClient httpClient, BeanValidator validator) {
        this.serviceURL = serviceURL;
        this.httpClient = httpClient;
        this.validator = validator;
    }

    public <T> T execute(HTTPMethod method, String path, Map<String, String> pathParams, Object requestBean, Type responseType) {
        logger.debug("call web service, serviceURL={}, method={}, path={}, pathParams={}", serviceURL, method, path, pathParams);
        validateRequestBean(requestBean);

        String serviceURL = serviceURL(path, pathParams);
        HTTPRequest httpRequest = new HTTPRequest(method, serviceURL);

        linkContext(httpRequest);

        if (requestBean != null) {
            String json = JSON.toJSON(requestBean);
            if (method == HTTPMethod.GET || method == HTTPMethod.DELETE) {
                Map<String, String> queryParams = JSON.fromJSON(Types.map(String.class, String.class), json);
                queryParams.forEach(httpRequest::addParam);
            } else if (method == HTTPMethod.POST || method == HTTPMethod.PUT) {
                httpRequest.text(json, ContentTypes.APPLICATION_JSON);
            } else {
                throw Exceptions.error("not supported method, method={}", method);
            }
        }

        if (signer != null) {
            logger.debug("sign request, signer={}", signer.getClass().getCanonicalName());
            signer.sign(httpRequest);
        }

        HTTPResponse response = httpClient.execute(httpRequest);
        validateResponse(response);

        if (void.class != responseType) {
            return JSON.fromJSON(responseType, response.text());
        } else {
            return null;
        }
    }

    private void linkContext(HTTPRequest httpRequest) {
        ActionLogContext.get(ActionLogContext.REQUEST_ID)
            .ifPresent(requestId -> httpRequest.header(HTTPServerHandler.HEADER_REQUEST_ID, requestId));

        ActionLogContext.get(ActionLogContext.TRACE)
            .ifPresent(trace -> {
                if ("true".equals(trace)) {
                    httpRequest.header(HTTPServerHandler.HEADER_TRACE, "true");
                }
            });
    }

    @Override
    public void signBy(WebServiceRequestSigner signer) {
        this.signer = signer;
    }

    private void validateRequestBean(Object requestBean) {
        if (requestBean != null) {
            try {
                validator.validate(requestBean);
            } catch (ValidationException e) {
                throw new Error(e);
            }
        }
    }

    private void validateResponse(HTTPResponse response) {
        HTTPStatus status = response.status();
        if (status.code >= HTTPStatus.OK.code && status.code <= 300) return;
        try {
            if (status == HTTPStatus.BAD_REQUEST) {
                ValidationErrorResponse validationErrorResponse = JSON.fromJSON(ValidationErrorResponse.class, response.text());
                throw new RemoteServiceException("failed to validate, message=" + validationErrorResponse.message + ", fieldErrors=" + validationErrorResponse.fieldErrors);
            } else {
                ErrorResponse errorResponse = JSON.fromJSON(ErrorResponse.class, response.text());
                throw new RemoteServiceException(errorResponse.message);
            }
        } catch (RemoteServiceException e) {
            throw e;
        } catch (Exception e) {
            logger.warn("failed to decode response, statusCode={}, responseText={}", status.code, response.text(), e);
            throw new RemoteServiceException("received non 2xx status code, status=" + status.code + ", remoteMessage=" + response.text(), e);
        }
    }

    private String serviceURL(String pathPattern, Map<String, String> pathParams) {
        StringBuilder builder = new StringBuilder(this.serviceURL);
        Path path = Path.parse(pathPattern).next; // skip the first '/'
        while (path != null) {
            String value = path.value;
            if ("/".equals(value)) builder.append(value);
            else if (value.startsWith(":")) {
                int paramIndex = value.indexOf('(');
                int endIndex = paramIndex > 0 ? paramIndex : value.length();
                String variable = value.substring(1, endIndex);
                builder.append('/').append(pathParams.get(variable));
            } else {
                builder.append('/').append(value);
            }
            path = path.next;
        }
        return builder.toString();
    }
}
