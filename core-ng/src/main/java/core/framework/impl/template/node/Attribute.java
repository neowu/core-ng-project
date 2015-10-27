package core.framework.impl.template.node;

import core.framework.api.util.Exceptions;
import core.framework.api.util.Strings;
import core.framework.impl.template.expression.CallTypeStack;
import core.framework.impl.template.fragment.ContainerFragment;
import core.framework.impl.template.fragment.EmptyAttributeFragment;
import core.framework.impl.template.fragment.HTMLContentFragment;
import core.framework.impl.template.fragment.MessageFragment;
import core.framework.impl.template.fragment.TextContentFragment;
import core.framework.impl.template.parser.HTMLParser;
import core.framework.impl.template.source.TemplateSource;

/**
 * @author neo
 */
public class Attribute {
    public final String name;
    public String value;
    public boolean hasDoubleQuote;
    public String location;

    public Attribute(String name) {
        this.name = name;
    }

    public boolean isDynamic() {
        return name.startsWith("c:");
    }

    void validate() {
        if (isDynamic() && !"c:cdn".equals(name) && Strings.isEmpty(value)) {
            throw Exceptions.error("dynamic attribute value must not be empty, attribute={}, location={}", name, location);
        }
    }

    void addStaticContent(ContainerFragment parent) {
        parent.addStaticContent(" ");
        parent.addStaticContent(name);
        if (value != null) {
            parent.addStaticContent("=");
            if (hasDoubleQuote) parent.addStaticContent("\"");
            parent.addStaticContent(value);
            if (hasDoubleQuote) parent.addStaticContent("\"");
        }
    }

    boolean isEmptyAttribute() {
        return "c:disabled".equals(name) || "c:checked".equals(name) || "c:selected".equals(name);
    }

    boolean isURLAttribute() {
        return "c:href".equals(name) || "href".equals(name) || "c:src".equals(name) || "src".equals(name);
    }

    void addEmptyAttribute(ContainerFragment parent, CallTypeStack stack) {
        parent.add(new EmptyAttributeFragment(name.substring(2), value, stack, location));
    }

    void addValueAttribute(ContainerFragment parent, CallTypeStack stack) {
        parent.addStaticContent(" ");
        parent.addStaticContent(name.substring(2));
        parent.addStaticContent("=\"");
        parent.add(new TextContentFragment(value, stack, location));
        parent.addStaticContent("\"");
    }

    void addDynamicContent(ContainerFragment parent, CallTypeStack stack, TemplateSource source) {
        if ("c:text".equals(name)) {
            parent.add(new TextContentFragment(value, stack, location));
        } else if ("c:html".equals(name)) {
            parent.add(new HTMLContentFragment(value, stack, location));
        } else if ("c:msg".equals(name)) {
            parent.add(new MessageFragment(value, stack, location));
        } else if ("c:include".equals(name)) {
            TemplateSource includedSource = source.resolve(value);
            Document document = new HTMLParser(includedSource).parse();
            document.buildTemplate(parent, stack, includedSource);
        }
    }
}
