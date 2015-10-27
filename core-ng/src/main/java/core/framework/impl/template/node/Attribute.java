package core.framework.impl.template.node;

import core.framework.api.util.Exceptions;
import core.framework.api.util.Strings;
import core.framework.impl.template.TemplateMetaContext;
import core.framework.impl.template.fragment.CDNFragment;
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
        if (isDynamic() && Strings.isEmpty(value)) {
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

    boolean isCDNAttribute(String tagName) {
        if ("link".equals(tagName) && ("c:href".equals(name) || "href".equals(name))) return true;
        if ("script".equals(tagName) && ("c:src".equals(name) || "src".equals(name))) return true;
        if ("img".equals(tagName) && ("c:src".equals(name) || "src".equals(name))) return true;
        return false;
    }

    void addCDNAttribute(ContainerFragment parent, TemplateMetaContext context) {
        if ("c:href".equals(name)) {
            parent.addStaticContent(" href=");
            parent.add(new CDNFragment(value, context, location));
        } else if ("href".equals(name)) {
            if (context.withCDN()) {
                parent.addStaticContent(" href=");
                parent.addStaticContent(context.cdn.url(value));
            } else {
                addStaticContent(parent);
            }
        } else if ("c:src".equals(name)) {
            parent.addStaticContent(" src=");
            parent.add(new CDNFragment(value, context, location));
        } else if ("src".equals(name)) {
            if (context.withCDN()) {
                parent.addStaticContent(" src=");
                parent.addStaticContent(context.cdn.url(value));
            } else {
                addStaticContent(parent);
            }
        }

    }

    void addEmptyAttribute(ContainerFragment parent, TemplateMetaContext context) {
        parent.add(new EmptyAttributeFragment(name.substring(2), value, context, location));
    }

    void addValueAttribute(ContainerFragment parent, TemplateMetaContext context) {
        parent.addStaticContent(" ");
        parent.addStaticContent(name.substring(2));
        parent.addStaticContent("=\"");
        parent.add(new TextContentFragment(value, context, location));
        parent.addStaticContent("\"");
    }

    void addDynamicContent(ContainerFragment parent, TemplateMetaContext context, TemplateSource source) {
        if ("c:text".equals(name)) {
            parent.add(new TextContentFragment(value, context, location));
        } else if ("c:html".equals(name)) {
            parent.add(new HTMLContentFragment(value, context, location));
        } else if ("c:msg".equals(name)) {
            parent.add(new MessageFragment(value, context, location));
        } else if ("c:include".equals(name)) {
            TemplateSource includedSource = source.resolve(value);
            Document document = new HTMLParser(includedSource).parse();
            document.buildTemplate(parent, context, includedSource);
        }
    }
}
