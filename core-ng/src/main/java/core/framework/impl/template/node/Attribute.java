package core.framework.impl.template.node;

import core.framework.api.util.Exceptions;
import core.framework.impl.template.TemplateMetaContext;
import core.framework.impl.template.fragment.BooleanAttributeFragment;
import core.framework.impl.template.fragment.ContainerFragment;
import core.framework.impl.template.fragment.HTMLContentFragment;
import core.framework.impl.template.fragment.TextContentFragment;
import core.framework.impl.template.fragment.URLFragment;
import core.framework.impl.template.parser.HTMLParser;
import core.framework.impl.template.source.TemplateSource;

/**
 * @author neo
 */
public class Attribute {
    public final String name;
    public final String tagName;
    public final String location;

    public String value;
    public boolean hasDoubleQuote;

    public Attribute(String name, String tagName, String location) {
        this.name = name;
        this.tagName = tagName;
        this.location = location;
    }

    public boolean isDynamic() {
        return name.startsWith("c:");
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

    boolean isDynamicBooleanAttribute() {
        return "c:checked".equals(name)
            || "c:selected".equals(name)
            || "c:disabled".equals(name)
            || "c:readonly".equals(name)
            || "c:multiple".equals(name)
            || "c:ismap".equals(name)
            || "c:defer".equals(name);
    }

    void addBooleanAttribute(ContainerFragment parent, TemplateMetaContext context) {
        parent.add(new BooleanAttributeFragment(name.substring(2), value, context, location));
    }

    boolean isCDNAttribute() {
        if ("link".equals(tagName) && ("c:href".equals(name) || "href".equals(name))) return true;
        if ("script".equals(tagName) && ("c:src".equals(name) || "src".equals(name))) return true;
        if ("img".equals(tagName) && ("c:src".equals(name) || "src".equals(name))) return true;
        return false;
    }

    void addCDNAttribute(ContainerFragment parent, TemplateMetaContext context) {
        String attributeName = isDynamic() ? name.substring(2) : name;
        if (isDynamic()) {
            parent.addStaticContent(" ");
            parent.addStaticContent(attributeName);
            parent.addStaticContent("=");
            parent.add(new URLFragment(value, context, true, location));
        } else {
            if (context.cdn != null) {  // expand cdn during compiling
                parent.addStaticContent(" ");
                parent.addStaticContent(attributeName);
                parent.addStaticContent("=");
                parent.addStaticContent(context.cdn.url(value));
            } else {
                addStaticContent(parent);
            }
        }
    }

    void addValueAttribute(ContainerFragment parent, TemplateMetaContext context) {
        parent.addStaticContent(" ");
        parent.addStaticContent(name.substring(2));
        if ("c:href".equals(name)) {
            parent.addStaticContent("=");
            parent.add(new URLFragment(value, context, false, location));
        } else {
            parent.addStaticContent("=\"");
            parent.add(new TextContentFragment(value, context, location));
            parent.addStaticContent("\"");
        }
    }

    void addDynamicContent(ContainerFragment parent, TemplateMetaContext context, TemplateSource source) {
        switch (name) {
            case "c:text":
                parent.add(new TextContentFragment(value, context, location));
                break;
            case "c:html":
                parent.add(new HTMLContentFragment(value, context, location));
                break;
            case "c:msg":
                if (context.message == null)
                    throw Exceptions.error("c:msg must be used with messages, location={}", location);
                String message = context.message.message(value, context.language).orElseThrow(() -> Exceptions.error("can not find message, key={}, location={}", value, location));
                parent.addStaticContent(message);
                break;
            case "c:include":
                TemplateSource includedSource = source.resolve(value);
                Document document = new HTMLParser(includedSource).parse();
                document.buildTemplate(parent, context, includedSource);
                break;
            default:
                throw Exceptions.error("not supported dynamic content attribute, name={}", name);
        }
    }
}
