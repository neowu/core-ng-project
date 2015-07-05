package core.framework.impl.web.response;

import core.framework.api.http.ContentTypes;
import core.framework.api.template.HTMLTemplate;
import core.framework.api.web.ResponseImpl;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

/**
 * @author neo
 */
public class HTMLBodyResponseHandler implements BodyHandler {
    private final HTMLTemplate htmlTemplate = new HTMLTemplate();

    @Override
    public void handle(ResponseImpl response, HttpServerExchange exchange) {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, ContentTypes.TEXT_HTML);

        HTMLBody body = (HTMLBody) response.body;
        String html = htmlTemplate.process(body.templateName, body.context);
        exchange.getResponseSender().send(html);
    }
}
