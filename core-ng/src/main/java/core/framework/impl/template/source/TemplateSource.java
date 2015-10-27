package core.framework.impl.template.source;

/**
 * @author neo
 */
public interface TemplateSource {
    String name();

    String content();

    TemplateSource resolve(String path);
}
