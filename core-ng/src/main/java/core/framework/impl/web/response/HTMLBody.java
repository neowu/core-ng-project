package core.framework.impl.web.response;

/**
 * @author neo
 */
public class HTMLBody implements Body {
    final String templateName;
    final Object model;

    public HTMLBody(String templateName, Object model) {
        this.templateName = templateName;
        this.model = model;
    }
}
