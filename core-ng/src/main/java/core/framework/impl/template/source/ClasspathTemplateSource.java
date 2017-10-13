package core.framework.impl.template.source;

import core.framework.util.ClasspathResources;

/**
 * @author neo
 */
public final class ClasspathTemplateSource implements TemplateSource {
    private final String classpath;

    public ClasspathTemplateSource(String classpath) {
        this.classpath = classpath;
    }

    @Override
    public String name() {
        return classpath;
    }

    @Override
    public String content() {
        return ClasspathResources.text(classpath);
    }

    @Override
    public TemplateSource resolve(String path) {
        return new ClasspathTemplateSource(path);
    }
}
