package core.framework.impl.template.node;

import core.framework.api.util.Exceptions;
import core.framework.api.util.Maps;
import core.framework.api.util.Sets;
import core.framework.impl.template.expression.CallTypeStack;
import core.framework.impl.template.fragment.ContainerFragment;
import core.framework.impl.template.fragment.URLFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author neo
 */
public class Attributes {
    private final Map<String, Attribute> attributes = Maps.newLinkedHashMap();
    private final Set<String> dynamicAttributes = Sets.newHashSet();

    public void add(Attribute attribute) {
        attributes.put(attribute.name, attribute);
        if (attribute.isDynamic()) dynamicAttributes.add(attribute.name);
    }

    public void buildTemplate(ContainerFragment parent, CallTypeStack stack) {
        validate();

        for (Map.Entry<String, Attribute> entry : attributes.entrySet()) {
            String name = entry.getKey();
            Attribute attribute = entry.getValue();
            if (skip(name)) continue;

            if (attribute.isURLAttribute()) {
                buildURLAttribute(parent, stack, attribute);
            } else if (attribute.isEmptyAttribute()) {
                attribute.addEmptyAttribute(parent, stack);
            } else if (attribute.isDynamic()) {
                attribute.addValueAttribute(parent, stack);
            } else {
                attribute.addStaticContent(parent);
            }
        }
    }

    private void buildURLAttribute(ContainerFragment parent, CallTypeStack stack, Attribute attribute) {
        String name = attribute.name;
        boolean hasCDN = dynamicAttributes.contains("c:cdn");

        if ("c:href".equals(name)) {
            parent.addStaticContent(" href=");
            parent.add(new URLFragment(attribute.value, stack, attribute.location, hasCDN));
        } else if ("href".equals(name)) {
            if (hasCDN) {
                parent.addStaticContent(" href=");
                parent.add(new URLFragment(attribute.value));
            } else {
                attribute.addStaticContent(parent);
            }
        } else if ("c:src".equals(name)) {
            parent.addStaticContent(" src=");
            parent.add(new URLFragment(attribute.value, stack, attribute.location, hasCDN));
        } else if ("src".equals(name)) {
            if (hasCDN) {
                parent.addStaticContent(" src=");
                parent.add(new URLFragment(attribute.value));
            } else {
                attribute.addStaticContent(parent);
            }
        }
    }

    private boolean skip(String name) {
        if ("xmlns:c".equals(name)
            || "c:text".equals(name)
            || "c:html".equals(name)
            || "c:msg".equals(name)
            || "c:include".equals(name)
            || "c:for".equals(name)
            || "c:if".equals(name)
            || "c:cdn".equals(name))
            return true;

        return dynamicAttributes.contains("c:" + name);
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
        attributes.values().forEach(core.framework.impl.template.node.Attribute::validate);

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
