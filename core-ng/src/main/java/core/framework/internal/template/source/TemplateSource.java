package core.framework.internal.template.source;

/**
 * @author neo
 */
public interface TemplateSource {
    String name();

    String content();

    TemplateSource resolve(String path);
}
