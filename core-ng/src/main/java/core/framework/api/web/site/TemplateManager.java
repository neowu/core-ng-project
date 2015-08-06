package core.framework.api.web.site;

import core.framework.api.web.Request;

/**
 * @author neo
 */
public interface TemplateManager {
    String process(String templatePath, Object model, Request request);
}
