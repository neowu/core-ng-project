package core.framework.impl.web.response;

/**
 * @author neo
 */
public class TemplateBody implements Body {
    final String templatePath;
    final Object model;

    public TemplateBody(String templatePath, Object model) {
        this.templatePath = templatePath;
        this.model = model;
    }
}
