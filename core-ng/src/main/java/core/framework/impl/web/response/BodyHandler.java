package core.framework.impl.web.response;

import core.framework.api.web.ResponseImpl;
import core.framework.impl.web.request.RequestImpl;
import io.undertow.io.Sender;

/**
 * @author neo
 */
public interface BodyHandler {
    void handle(ResponseImpl response, Sender sender, RequestImpl request);
}
