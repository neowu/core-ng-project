package core.framework.impl.template.location;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

/**
 * @author neo
 */
public final class StringTemplateLocation implements TemplateLocation {
    public final String template;

    public StringTemplateLocation(String template) {
        this.template = template;
    }

    @Override
    public BufferedReader reader() throws IOException {
        return new BufferedReader(new StringReader(template));
    }

    @Override
    public TemplateLocation location(String path) {
        throw new Error("string template does not support include");
    }
}
