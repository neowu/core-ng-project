package core.framework.impl.web.response;

/**
 * @author neo
 */
public class HTMLBody implements Body {
    final String templatePath;
    final Object model;

    public HTMLBody(String templatePath, Object model) {
        this.templatePath = templatePath;
        this.model = model;
    }
}
