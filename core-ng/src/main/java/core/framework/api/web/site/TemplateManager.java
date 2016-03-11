package core.framework.api.web.site;

/**
 * @author neo
 */
public interface TemplateManager {
    String process(String templatePath, Object model, String language);
}
