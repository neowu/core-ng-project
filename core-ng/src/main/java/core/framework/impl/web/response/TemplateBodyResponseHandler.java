package core.framework.impl.web.response;

import core.framework.api.web.ResponseImpl;
import core.framework.impl.web.TemplateManager;
import io.undertow.server.HttpServerExchange;

/**
 * @author neo
 */
public class TemplateBodyResponseHandler implements BodyHandler {
    private final TemplateManager templateManager;

    public TemplateBodyResponseHandler(TemplateManager templateManager) {
        this.templateManager = templateManager;
    }

    @Override
    public void handle(ResponseImpl response, HttpServerExchange exchange) {
        TemplateBody body = (TemplateBody) response.body;
        String html = templateManager.process(body.templatePath, body.model);
        exchange.getResponseSender().send(html);
    }
}
