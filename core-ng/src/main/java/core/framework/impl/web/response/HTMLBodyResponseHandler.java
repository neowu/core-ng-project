package core.framework.impl.web.response;

import core.framework.api.http.ContentTypes;
import core.framework.api.web.ResponseImpl;
import core.framework.impl.web.HTMLTemplateManager;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

/**
 * @author neo
 */
public class HTMLBodyResponseHandler implements BodyHandler {
    private final HTMLTemplateManager templateManager;

    public HTMLBodyResponseHandler(HTMLTemplateManager templateManager) {
        this.templateManager = templateManager;
    }

    @Override
    public void handle(ResponseImpl response, HttpServerExchange exchange) {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, ContentTypes.TEXT_HTML);

        HTMLBody body = (HTMLBody) response.body;
        String html = templateManager.process(body.templateName, body.model);
        exchange.getResponseSender().send(html);
    }
}
