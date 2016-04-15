package core.framework.impl.web.service;

import core.framework.api.http.HTTPMethod;
import core.framework.api.web.service.WebServiceRequestSigner;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * @author neo
 */
public interface WebServiceClient { // defined all methods be called in generated code which must be public
    String serviceURL(String pathPattern, Map<String, Object> pathParams);

    Object execute(HTTPMethod method, String serviceURL, Type requestType, Object requestBean, Type responseType);

    void signBy(WebServiceRequestSigner signer);
}
