package core.framework.internal.template;

import core.framework.internal.template.node.Document;
import core.framework.internal.template.parser.HTMLParser;
import core.framework.internal.template.source.TemplateSource;

/**
 * @author neo
 */
public class HTMLTemplateBuilder {
    private final TemplateSource source;
    private final Class<?> modelClass;
    private final Document document;

    public CDNManager cdn;
    public MessageProvider message;

    public HTMLTemplateBuilder(TemplateSource source, Class<?> modelClass) {
        new ModelClassValidator(modelClass).validate();
        this.source = source;
        this.modelClass = modelClass;
        document = new HTMLParser(source).parse();
    }

    public HTMLTemplate build() {
        TemplateMetaContext context = new TemplateMetaContext(modelClass);
        context.cdn = cdn;
        context.message = message;
        HTMLTemplate template = new HTMLTemplate(context.rootClass);
        document.buildTemplate(template, context, source);
        return template;
    }
}
