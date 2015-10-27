package core.framework.impl.template;

import core.framework.impl.template.node.Document;
import core.framework.impl.template.parser.HTMLParser;
import core.framework.impl.template.source.TemplateSource;

/**
 * @author neo
 */
public class HTMLTemplateBuilder {
    private final TemplateSource source;
    private final TemplateMetaContext context;

    public HTMLTemplateBuilder(TemplateSource source, Class<?> modelClass) {
        new ModelClassValidator(modelClass).validate();
        this.source = source;
        this.context = new TemplateMetaContext(modelClass);
    }

    public void cdn(CDNFunction function) {
        context.cdn = function;
    }

    public HTMLTemplate build() {
        HTMLTemplate template = new HTMLTemplate(context.rootClass);
        Document document = new HTMLParser(source).parse();
        document.buildTemplate(template, context, source);
        return template;
    }
}
