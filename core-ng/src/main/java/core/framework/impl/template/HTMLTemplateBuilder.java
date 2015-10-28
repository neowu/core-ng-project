package core.framework.impl.template;

import core.framework.impl.template.node.Document;
import core.framework.impl.template.parser.HTMLParser;
import core.framework.impl.template.source.TemplateSource;

/**
 * @author neo
 */
public class HTMLTemplateBuilder {
    private final TemplateSource source;
    private final Class<?> modelClass;

    public CDNManager cdn;
    public MessageManager message;
    public String language;

    private Document document;

    public HTMLTemplateBuilder(TemplateSource source, Class<?> modelClass) {
        new ModelClassValidator(modelClass).validate();
        this.source = source;
        this.modelClass = modelClass;
    }

    public void parse() {
        document = new HTMLParser(source).parse();
    }

    public HTMLTemplate build() {
        TemplateMetaContext context = new TemplateMetaContext(modelClass);
        context.cdn = cdn;
        context.message = message;
        context.language = language;
        HTMLTemplate template = new HTMLTemplate(context.rootClass);
        document.buildTemplate(template, context, source);
        return template;
    }
}
