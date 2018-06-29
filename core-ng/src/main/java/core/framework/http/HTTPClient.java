package core.framework.http;

/**
 * @author neo
 */
public interface HTTPClient {
    HTTPResponse execute(HTTPRequest request);

    void close();
}
