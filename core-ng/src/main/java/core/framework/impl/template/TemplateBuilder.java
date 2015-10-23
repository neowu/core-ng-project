package core.framework.impl.template;

import core.framework.impl.template.expression.CallTypeStack;
import core.framework.impl.template.html.HTMLParser;
import core.framework.impl.template.html.node.Document;
import core.framework.impl.template.source.TemplateSource;

/**
 * @author neo
 */
public class TemplateBuilder {
    private final TemplateSource source;
    private final CallTypeStack stack;

    public TemplateBuilder(TemplateSource source, Class<?> modelClass) {
        new ModelClassValidator(modelClass).validate();
        this.source = source;
        this.stack = new CallTypeStack(modelClass);
    }

    public Template build() {
        Template template = new Template(stack.rootClass);
        Document document = new HTMLParser(source).parse();
        document.buildTemplate(template, stack, source);
        return template;
    }
}
