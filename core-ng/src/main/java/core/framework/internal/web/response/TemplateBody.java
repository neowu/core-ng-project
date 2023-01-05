package core.framework.internal.web.response;

import io.undertow.io.Sender;

/**
 * @author neo
 */
public final class TemplateBody implements Body {
    private final String templatePath;
    private final Object model;
    private final String language;

    public TemplateBody(String templatePath, Object model, String language) {
        this.templatePath = templatePath;
        this.model = model;
        this.language = language;
    }

    @Override
    public long send(Sender sender, ResponseHandlerContext context) {
        String content = context.templateManager.process(templatePath, model, language);
        sender.send(content);
        return content.length();
    }
}
