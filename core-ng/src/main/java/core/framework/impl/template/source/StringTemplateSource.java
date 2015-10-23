package core.framework.impl.template.source;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

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
    public String content() {
        return template;
    }

    @Override
    public BufferedReader reader() throws IOException {
        return new BufferedReader(new StringReader(template));
    }

    @Override
    public TemplateSource resolve(String path) {
        throw new Error("string template does not support include");
    }

    @Override
    public String source() {
        return name;
    }
}
