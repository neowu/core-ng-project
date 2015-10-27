package core.framework.impl.template.node;

import core.framework.impl.template.TemplateMetaContext;
import core.framework.impl.template.fragment.ContainerFragment;
import core.framework.impl.template.fragment.ForFragment;
import core.framework.impl.template.fragment.IfFragment;
import core.framework.impl.template.source.TemplateSource;

/**
 * @author neo
 */
public class Element extends ContainerNode {
    public final Attributes attributes;
    public final String name;
    public boolean startTagClosed;
    public boolean hasEndTag;

    public Element(String name) {
        this.name = name;
        attributes = new Attributes(name);
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
        currentParent.addStaticContent(name);

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
        if (startTagClosed) parent.addStaticContent("/>");
        else parent.addStaticContent(">");

        for (Node node : nodes) {
            node.buildTemplate(parent, context, source);
        }

        if (hasEndTag) addEndTag(parent);
    }

    private void addEndTag(ContainerFragment parent) {
        parent.addStaticContent("</");
        parent.addStaticContent(name);
        parent.addStaticContent(">");
    }
}
