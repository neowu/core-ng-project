package core.framework.impl.web.response;

import core.framework.api.web.ResponseImpl;
import core.framework.impl.web.RequestImpl;
import io.undertow.io.Sender;

/**
 * @author neo
 */
public class TextBodyResponseHandler implements BodyHandler {
    @Override
    public void handle(ResponseImpl response, Sender sender, RequestImpl request) {
        TextBody body = (TextBody) response.body;
        sender.send(body.text);
    }
}
