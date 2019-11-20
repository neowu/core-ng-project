package core.framework.web.service;

import core.framework.http.HTTPRequest;
import core.framework.http.HTTPResponse;

/**
 * @author neo
 */
public interface WebServiceClientInterceptor {
    default void onRequest(HTTPRequest request) {
    }

    default void onResponse(HTTPResponse response) {
    }
}
