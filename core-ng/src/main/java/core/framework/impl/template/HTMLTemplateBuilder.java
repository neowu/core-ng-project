package core.framework.impl.template;

import core.framework.impl.template.expression.CallTypeStack;
import core.framework.impl.template.node.Document;
import core.framework.impl.template.parser.HTMLParser;
import core.framework.impl.template.source.TemplateSource;

/**
 * @author neo
 */
public class HTMLTemplateBuilder {
    private final TemplateSource source;
    private final CallTypeStack stack;

    public HTMLTemplateBuilder(TemplateSource source, Class<?> modelClass) {
        new ModelClassValidator(modelClass).validate();
        this.source = source;
        this.stack = new CallTypeStack(modelClass);
    }

    public HTMLTemplate build() {
        HTMLTemplate template = new HTMLTemplate(stack.rootClass);
        Document document = new HTMLParser(source).parse();
        document.buildTemplate(template, stack, source);
        return template;
    }
}
