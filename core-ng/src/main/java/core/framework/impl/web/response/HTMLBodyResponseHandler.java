package core.framework.impl.web.response;

import core.framework.api.web.ResponseImpl;
import core.framework.impl.web.TemplateManager;
import io.undertow.server.HttpServerExchange;

/**
 * @author neo
 */
public class HTMLBodyResponseHandler implements BodyHandler {
    private final TemplateManager templateManager;

    public HTMLBodyResponseHandler(TemplateManager templateManager) {
        this.templateManager = templateManager;
    }

    @Override
    public void handle(ResponseImpl response, HttpServerExchange exchange) {
        HTMLBody body = (HTMLBody) response.body;
        String html = templateManager.process(body.templatePath, body.model);
        exchange.getResponseSender().send(html);
    }
}
