package core.framework.impl.web.response;

import java.util.Map;

/**
 * @author neo
 */
public class HTMLBody implements Body {
    final String templateName;
    final Map<String, Object> context;

    public HTMLBody(String templateName, Map<String, Object> context) {
        this.templateName = templateName;
        this.context = context;
    }
}
