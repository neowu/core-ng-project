package core.framework.impl.template.html.node;

import core.framework.api.util.Exceptions;
import core.framework.api.util.Maps;
import core.framework.api.util.Sets;
import core.framework.api.util.Strings;
import core.framework.impl.template.expression.CallTypeStack;
import core.framework.impl.template.fragment.ContainerFragment;
import core.framework.impl.template.fragment.EmptyAttributeFragment;
import core.framework.impl.template.fragment.TextContentFragment;
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
        if (attribute.name.startsWith("c:")) dynamicAttributes.add(attribute.name);
    }

    public void buildTemplate(ContainerFragment fragment, CallTypeStack stack) {
        validateDynamicContentAttribute();

        for (Map.Entry<String, Attribute> entry : attributes.entrySet()) {
            String name = entry.getKey();
            Attribute attr = entry.getValue();
            if (skip(name)) continue;

            if (isURL(name)) {
                buildURLAttribute(fragment, stack, attr);
            } else if (isEmptyAttribute(name)) {
                fragment.fragments.add(new EmptyAttributeFragment(name.substring(2), attr.value, stack, attr.location));
            } else if (name.startsWith("c:")) {
                buildAttribute(name.substring(2), fragment, stack, attr);
            } else {
                attr.addStaticContent(fragment);
            }
        }
    }

    private boolean isEmptyAttribute(String name) {
        return "c:disabled".equals(name) || "c:checked".equals(name) || "c:selected".equals(name);
    }

    private void buildURLAttribute(ContainerFragment fragment, CallTypeStack stack, Attribute attr) {
        String name = attr.name;
        boolean hasCDN = dynamicAttributes.contains("c:cdn");

        if ("c:href".equals(name)) {
            fragment.addStaticContent(" href=");
            fragment.fragments.add(new URLFragment(attr.value, stack, attr.location, hasCDN));
        } else if ("href".equals(name)) {
            if (hasCDN) {
                fragment.addStaticContent(" href=");
                fragment.fragments.add(new URLFragment(attr.value));
            } else {
                attr.addStaticContent(fragment);
            }
        } else if ("c:src".equals(name)) {
            fragment.addStaticContent(" src=");
            fragment.fragments.add(new URLFragment(attr.value, stack, attr.location, hasCDN));
        } else if ("src".equals(name)) {
            if (hasCDN) {
                fragment.addStaticContent(" src=");
                fragment.fragments.add(new URLFragment(attr.value));
            } else {
                attr.addStaticContent(fragment);
            }
        }
    }

    private boolean isURL(String name) {
        return "c:href".equals(name) || "href".equals(name) || "c:src".equals(name) || "src".equals(name);
    }

    private void buildAttribute(String name, ContainerFragment fragment, CallTypeStack stack, Attribute attr) {
        if (Strings.isEmpty(attr.value))
            throw Exceptions.error("dynamic attribute value must not be empty, attr={}, location={}", attr.name, attr.location);

        fragment.addStaticContent(" ");
        fragment.addStaticContent(name);
        fragment.addStaticContent("=\"");
        fragment.fragments.add(new TextContentFragment(attr.value, stack, attr.location));
        fragment.addStaticContent("\"");
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
        attribute = attributes.get("c:include");
        if (attribute != null) return attribute;
        throw new Error("can not find dynamic content attribute");
    }

    public List<Attribute> flowAttributes() {
        List<Attribute> attributes = new ArrayList<>();
        this.attributes.forEach((name, attr) -> {
            if ("c:if".equals(name) || "c:for".equals(name)) attributes.add(attr);
        });
        return attributes;
    }

    private void validateDynamicContentAttribute() {
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
