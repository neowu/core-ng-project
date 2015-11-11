package core.framework.impl.template.node;

import core.framework.api.util.Exceptions;
import core.framework.api.util.Maps;
import core.framework.impl.template.TemplateMetaContext;
import core.framework.impl.template.fragment.ContainerFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public class Attributes {
    public final Map<String, Attribute> attributes = Maps.newLinkedHashMap();

    public void add(Attribute attribute) {
        attributes.put(attribute.name, attribute);
    }

    public void buildTemplate(ContainerFragment parent, TemplateMetaContext context) {
        validate();

        for (Attribute attribute : attributes.values()) {
            if (skip(attribute)) continue;

            if (attribute.isCDNAttribute()) {
                attribute.addCDNAttribute(parent, context);
            } else if (attribute.isDynamic()) {
                if (attribute.isDynamicBooleanAttribute()) attribute.addBooleanAttribute(parent, context);
                else attribute.addValueAttribute(parent, context);
            } else {
                attribute.addStaticContent(parent);
            }
        }
    }

    private boolean skip(Attribute attribute) {
        String name = attribute.name;

        if ("xmlns:c".equals(name)
            || "c:text".equals(name)
            || "c:html".equals(name)
            || "c:msg".equals(name)
            || "c:include".equals(name)
            || "c:for".equals(name)
            || "c:if".equals(name))
            return true;

        return !attribute.isDynamic() && attributes.containsKey("c:" + name);   // there is dynamic attribute to overwrite
    }

    public boolean containDynamicContent() {
        return attributes.containsKey("c:text")
            || attributes.containsKey("c:msg")
            || attributes.containsKey("c:html")
            || attributes.containsKey("c:include");
    }

    public Attribute dynamicContentAttribute() {
        Attribute attribute = attributes.get("c:text");
        if (attribute != null) return attribute;
        attribute = attributes.get("c:msg");
        if (attribute != null) return attribute;
        attribute = attributes.get("c:html");
        if (attribute != null) return attribute;
        return attributes.get("c:include");
    }

    public List<Attribute> flowAttributes() {
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
        attribute = attributes.get("c:msg");
        if (attribute != null) count++;
        attribute = attributes.get("c:html");
        if (attribute != null) count++;
        attribute = attributes.get("c:include");
        if (attribute != null) count++;

        if (count > 1 && attribute != null)
            throw Exceptions.error("element must not have more than one dynamic content attribute, attribute={}, location={}", attribute.name, attribute.location);
    }
}
