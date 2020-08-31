package core.framework.internal.template.node;

import core.framework.internal.template.TemplateMetaContext;
import core.framework.internal.template.fragment.BooleanAttributeFragment;
import core.framework.internal.template.fragment.ContainerFragment;
import core.framework.internal.template.fragment.HTMLContentFragment;
import core.framework.internal.template.fragment.TextContentFragment;
import core.framework.internal.template.fragment.URLFragment;
import core.framework.internal.template.parser.HTMLParser;
import core.framework.internal.template.source.TemplateSource;

import static core.framework.util.Strings.format;

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

    boolean isDynamic() {
        return name.startsWith("c:");
    }

    boolean isMessage() {
        return name.startsWith("m:");
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
        parent.addStaticContent(name.substring(name.lastIndexOf(':') + 1));
        if ("c:href".equals(name)) {
            parent.addStaticContent("=");
            parent.add(new URLFragment(value, context, false, location));
        } else if (name.startsWith("c:html:")) {
            parent.addStaticContent("=\"");
            parent.add(new HTMLContentFragment(value, context, location));
            parent.addStaticContent("\"");
        } else {
            parent.addStaticContent("=\"");
            parent.add(new TextContentFragment(value, context, location));
            parent.addStaticContent("\"");
        }
    }

    void addMessageAttribute(ContainerFragment parent, TemplateMetaContext context) {
        String message = context.message.get(value).orElseThrow(() -> new Error(format("can not find message, key={}, location={}", value, location)));
        parent.addStaticContent(" ");
        parent.addStaticContent(name.substring(2));
        parent.addStaticContent("=\"");
        parent.addStaticContent(message);
        parent.addStaticContent("\"");
    }

    void addDynamicContent(ContainerFragment parent, TemplateMetaContext context, TemplateSource source) {
        switch (name) {
            case "c:text" -> parent.add(new TextContentFragment(value, context, location));
            case "c:html" -> parent.add(new HTMLContentFragment(value, context, location));
            case "m:text" -> {
                String message = context.message.get(value).orElseThrow(() -> new Error(format("can not find message, key={}, location={}", value, location)));
                parent.addStaticContent(message);
            }
            case "c:include" -> {
                TemplateSource includedSource = source.resolve(value);
                Document document = new HTMLParser(includedSource).parse();
                document.buildTemplate(parent, context, includedSource);
            }
            default -> throw new Error("not supported dynamic content attribute, name=" + name);
        }
    }
}
