package core.framework.http;

/**
 * @author neo
 */
public interface HTTPClient {
    static HTTPClientBuilder builder() {
        return new HTTPClientBuilder();
    }

    HTTPResponse execute(HTTPRequest request);
}
