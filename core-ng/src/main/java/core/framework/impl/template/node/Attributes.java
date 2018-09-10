package core.framework.impl.template.node;

import core.framework.impl.template.TemplateMetaContext;
import core.framework.impl.template.fragment.ContainerFragment;
import core.framework.util.Maps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public class Attributes {
    public final Map<String, Attribute> attributes = Maps.newLinkedHashMap();

    public void add(Attribute attribute) {
        attributes.put(attribute.name, attribute);
    }

    void buildTemplate(ContainerFragment parent, TemplateMetaContext context) {
        validate();

        for (Attribute attribute : attributes.values()) {
            if (skip(attribute)) continue;

            if (attribute.isCDNAttribute()) {
                attribute.addCDNAttribute(parent, context);
            } else if (attribute.isDynamic()) {
                if (attribute.isDynamicBooleanAttribute()) attribute.addBooleanAttribute(parent, context);
                else attribute.addValueAttribute(parent, context);
            } else if (attribute.isMessage()) {
                attribute.addMessageAttribute(parent, context);
            } else {
                attribute.addStaticContent(parent);
            }
        }
    }

    private boolean skip(Attribute attribute) {
        String name = attribute.name;

        if ("xmlns:c".equals(name) || "xmlns:m".equals(name)
            || "c:text".equals(name)
            || "c:html".equals(name)
            || "m:text".equals(name)
            || "c:include".equals(name)
            || "c:for".equals(name)
            || "c:if".equals(name))
            return true;

        return !attribute.isDynamic() && (attributes.containsKey("c:" + name) || attributes.containsKey("m:" + name));   // there is dynamic attribute to overwrite
    }

    boolean containDynamicContent() {
        return attributes.containsKey("c:text")
            || attributes.containsKey("m:text")
            || attributes.containsKey("c:html")
            || attributes.containsKey("c:include");
    }

    Attribute dynamicContentAttribute() {
        Attribute attribute = attributes.get("c:text");
        if (attribute != null) return attribute;
        attribute = attributes.get("m:text");
        if (attribute != null) return attribute;
        attribute = attributes.get("c:html");
        if (attribute != null) return attribute;
        return attributes.get("c:include");
    }

    List<Attribute> flowAttributes() {
        List<Attribute> attributes = new ArrayList<>();
        this.attributes.forEach((name, attr) -> {
            if ("c:if".equals(name) || "c:for".equals(name)) attributes.add(attr);
        });
        return attributes;
    }

    private void validate() {
        int count = 0;

        Attribute attribute = attributes.get("c:text");
        if (attribute != null) count++;
        attribute = attributes.get("m:text");
        if (attribute != null) count++;
        attribute = attributes.get("c:html");
        if (attribute != null) count++;
        attribute = attributes.get("c:include");
        if (attribute != null) count++;

        if (count > 1 && attribute != null)
            throw new Error(format("element must not have more than one dynamic content attribute, attribute={}, location={}", attribute.name, attribute.location));
    }
}
