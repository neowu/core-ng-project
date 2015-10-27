package core.framework.impl.template.source;

/**
 * @author neo
 */
public final class StringTemplateSource implements TemplateSource {
    public final String name;
    public final String template;

    public StringTemplateSource(String name, String template) {
        this.name = name;
        this.template = template;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String content() {
        return template;
    }

    @Override
    public TemplateSource resolve(String path) {
        throw new Error("string template does not support include");
    }
}
