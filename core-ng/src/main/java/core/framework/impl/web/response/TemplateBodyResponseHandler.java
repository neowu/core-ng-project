package core.framework.impl.web.response;

import core.framework.api.web.ResponseImpl;
import core.framework.api.web.site.TemplateManager;
import core.framework.impl.web.request.RequestImpl;
import io.undertow.io.Sender;

/**
 * @author neo
 */
public class TemplateBodyResponseHandler implements BodyHandler {
    private final TemplateManager templateManager;

    public TemplateBodyResponseHandler(TemplateManager templateManager) {
        this.templateManager = templateManager;
    }

    @Override
    public void handle(ResponseImpl response, Sender sender, RequestImpl request) {
        TemplateBody body = (TemplateBody) response.body;
        String content = templateManager.process(body.templatePath, body.model, request);
        sender.send(content);
    }
}
