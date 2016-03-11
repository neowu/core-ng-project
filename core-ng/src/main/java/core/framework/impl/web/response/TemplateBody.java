package core.framework.impl.web.response;

/**
 * @author neo
 */
public class TemplateBody implements Body {
    final String templatePath;
    final Object model;
    final String language;

    public TemplateBody(String templatePath, Object model, String language) {
        this.templatePath = templatePath;
        this.model = model;
        this.language = language;
    }
}
