package core.framework.impl.template.location;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

/**
 * @author neo
 */
public final class StringTemplateSource implements TemplateSource {
    public final String template;

    public StringTemplateSource(String template) {
        this.template = template;
    }

    @Override
    public BufferedReader reader() throws IOException {
        return new BufferedReader(new StringReader(template));
    }

    @Override
    public TemplateSource resolve(String path) {
        throw new Error("string template does not support include");
    }
}
