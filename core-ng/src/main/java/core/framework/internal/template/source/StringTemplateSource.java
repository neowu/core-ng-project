package core.framework.internal.template.source;

/**
 * @author neo
 */
public final class StringTemplateSource implements TemplateSource {
    private final String name;
    private final String template;

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
