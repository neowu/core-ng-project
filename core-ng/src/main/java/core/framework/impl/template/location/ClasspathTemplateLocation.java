package core.framework.impl.template.location;

import core.framework.api.util.ClasspathResources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

/**
 * @author neo
 */
public final class ClasspathTemplateLocation implements TemplateLocation {
    public final String path;

    public ClasspathTemplateLocation(String path) {
        this.path = path;
    }

    @Override
    public BufferedReader reader() throws IOException {
        return new BufferedReader(new StringReader(ClasspathResources.text(path)));
    }

    @Override
    public TemplateLocation location(String path) {
        return new ClasspathTemplateLocation(path);
    }

    @Override
    public String toString() {
        return "classpath:" + path;
    }
}
