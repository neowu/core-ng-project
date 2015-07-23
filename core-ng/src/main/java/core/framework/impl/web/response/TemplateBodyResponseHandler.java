package core.framework.impl.web.response;

import core.framework.api.web.ResponseImpl;
import core.framework.impl.web.RequestImpl;
import core.framework.impl.web.template.TemplateManager;
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
        String html = templateManager.process(body.templatePath, body.model, request);
        sender.send(html);
    }
}
