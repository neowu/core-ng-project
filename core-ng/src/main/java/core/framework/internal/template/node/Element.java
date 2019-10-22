package core.framework.internal.template.node;

import core.framework.internal.template.TemplateMetaContext;
import core.framework.internal.template.fragment.ContainerFragment;
import core.framework.internal.template.fragment.ForFragment;
import core.framework.internal.template.fragment.IfFragment;
import core.framework.internal.template.source.TemplateSource;

/**
 * @author neo
 */
public class Element extends ContainerNode {
    public final Attributes attributes = new Attributes();
    public final String name;
    public boolean hasEndTag;

    public Element(String name) {
        this.name = name;
    }

    @Override
    public void buildTemplate(ContainerFragment parent, TemplateMetaContext context, TemplateSource source) {
        ContainerFragment currentParent = parent;
        ForFragment forFragment = null;
        for (Attribute attribute : attributes.flowAttributes()) {
            if ("c:if".equals(attribute.name)) {
                IfFragment ifFragment = new IfFragment(attribute.value, context, attribute.location);
                currentParent.add(ifFragment);
                currentParent = ifFragment;
            } else if ("c:for".equals(attribute.name)) {
                forFragment = new ForFragment(attribute.value, context, attribute.location);
                context.paramClasses.put(forFragment.variable, forFragment.valueClass);
                currentParent.add(forFragment);
                currentParent = forFragment;
            }
        }

        currentParent.addStaticContent("<");

        if ("template".equals(name)) {
            currentParent.addStaticContent("script type=\"text/template\"");
        } else {
            currentParent.addStaticContent(name);
        }

        attributes.buildTemplate(currentParent, context);

        if (attributes.containDynamicContent()) {
            currentParent.addStaticContent(">");

            Attribute attribute = attributes.dynamicContentAttribute();
            attribute.addDynamicContent(currentParent, context, source);

            addEndTag(currentParent);
        } else {
            buildStaticContent(currentParent, context, source);
        }

        if (forFragment != null) {
            context.paramClasses.remove(forFragment.variable);
        }
    }

    private void buildStaticContent(ContainerFragment parent, TemplateMetaContext context, TemplateSource source) {
        parent.addStaticContent(">");

        for (Node node : nodes) {
            node.buildTemplate(parent, context, source);
        }

        if (hasEndTag) addEndTag(parent);
    }

    private void addEndTag(ContainerFragment parent) {
        parent.addStaticContent("</");
        if ("template".equals(name)) {
            parent.addStaticContent("script");
        } else {
            parent.addStaticContent(name);
        }
        parent.addStaticContent(">");
    }
}
