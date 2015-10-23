package core.framework.impl.template.source;

/**
 * @author neo
 */
public interface TemplateSource {
    String content();

    TemplateSource resolve(String path);

    String source();
}
