package core.framework.impl.template.html.node;

import core.framework.api.util.Maps;
import core.framework.impl.template.expression.CallTypeStack;
import core.framework.impl.template.fragment.ContainerFragment;
import core.framework.impl.template.fragment.TextContentFragment;
import core.framework.impl.template.fragment.URLFragment;

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

    public void buildTemplate(ContainerFragment fragment, CallTypeStack stack) {
        boolean hasDynamicSrc = attributes.containsKey("c:src");
        boolean hasDynamicClass = attributes.containsKey("c:class");
        boolean hasDynamicHref = attributes.containsKey("c:href");
        boolean hasCDN = attributes.containsKey("c:cdn");

        attributes.forEach((name, attr) -> {
            if ("xmlns:c".equals(name)
                || "c:text".equals(name)
                || "c:html".equals(name)
                || "c:msg".equals(name)
                || "c:include".equals(name)
                || "c:for".equals(name)
                || "c:if".equals(name))
                return;
            if ("class".equals(name) && hasDynamicClass) return;
            if ("src".equals(name) && hasDynamicSrc) return;
            if ("href".equals(name) && hasDynamicHref) return;

            if ("c:class".equals(name)) {
                fragment.addStaticContent(" class=\"");
                fragment.fragments.add(new TextContentFragment(attr.value, stack, attr.location));
                fragment.addStaticContent("\"");
            } else if ("c:href".equals(name)) {
                fragment.addStaticContent(" href=");
                fragment.fragments.add(new URLFragment(attr.value, stack, attr.location, hasCDN));
            } else if ("href".equals(name) && hasCDN) {
                fragment.addStaticContent(" href=");
                fragment.fragments.add(new URLFragment(attr.value));
            } else if ("c:src".equals(name)) {
                fragment.addStaticContent(" src=");
                fragment.fragments.add(new URLFragment(attr.value, stack, attr.location, hasCDN));
            } else if ("src".equals(name) && hasCDN) {
                fragment.addStaticContent(" src=");
                fragment.fragments.add(new URLFragment(attr.value));
            } else {
                attr.addStaticContent(fragment);
            }
        });
    }

    public List<Attribute> flowAttributes() {
        List<Attribute> attributes = new ArrayList<>();
        this.attributes.forEach((name, attr) -> {
            if ("c:if".equals(name) || "c:for".equals(name)) attributes.add(attr);
        });
        return attributes;
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

    public boolean isContentDynamic() {
        return attributes.containsKey("c:text")
            || attributes.containsKey("c:msg")
            || attributes.containsKey("c:html")
            || attributes.containsKey("c:include");
    }
}
