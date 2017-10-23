package core.framework.http;

/**
 * @author neo
 */
public interface HTTPClient extends AutoCloseable {
    HTTPResponse execute(HTTPRequest request);

    @Override
    void close();
}
