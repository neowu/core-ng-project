package core.framework.impl.web.response;

import core.framework.impl.web.request.RequestImpl;
import core.framework.impl.web.site.TemplateManager;
import io.undertow.io.Sender;

/**
 * @author neo
 */
class TemplateBodyResponseHandler implements BodyHandler {
    private final TemplateManager templateManager;

    TemplateBodyResponseHandler(TemplateManager templateManager) {
        this.templateManager = templateManager;
    }

    @Override
    public void handle(ResponseImpl response, Sender sender, RequestImpl request) {
        TemplateBody body = (TemplateBody) response.body;
        String content = templateManager.process(body.templatePath, body.model, body.language);
        sender.send(content);
    }
}
