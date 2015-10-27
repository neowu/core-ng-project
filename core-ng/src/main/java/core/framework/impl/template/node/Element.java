package core.framework.impl.template.node;

import core.framework.impl.template.expression.CallTypeStack;
import core.framework.impl.template.fragment.ContainerFragment;
import core.framework.impl.template.fragment.ForFragment;
import core.framework.impl.template.fragment.IfFragment;
import core.framework.impl.template.source.TemplateSource;

/**
 * @author neo
 */
public class Element extends ContainerNode {
    public final Attributes attributes = new Attributes();
    public final String name;
    public boolean startTagClosed;
    public boolean hasEndTag;

    public Element(String name) {
        this.name = name;
    }

    @Override
    public void buildTemplate(ContainerFragment parent, CallTypeStack stack, TemplateSource source) {
        ContainerFragment currentParent = parent;
        ForFragment forFragment = null;
        for (Attribute attribute : attributes.flowAttributes()) {
            if ("c:if".equals(attribute.name)) {
                IfFragment ifFragment = new IfFragment(attribute.value, stack, attribute.location);
                currentParent.add(ifFragment);
                currentParent = ifFragment;
            } else if ("c:for".equals(attribute.name)) {
                forFragment = new ForFragment(attribute.value, stack, attribute.location);
                stack.paramClasses.put(forFragment.variable, forFragment.valueClass);
                currentParent.add(forFragment);
                currentParent = forFragment;
            }
        }

        currentParent.addStaticContent("<");
        currentParent.addStaticContent(name);

        attributes.buildTemplate(currentParent, stack);

        if (attributes.containDynamicContent()) {
            currentParent.addStaticContent(">");

            Attribute attribute = attributes.dynamicContentAttribute();
            attribute.addDynamicContent(currentParent, stack, source);

            addEndTag(currentParent);
        } else {
            buildStaticContent(currentParent, stack, source);
        }

        if (forFragment != null) {
            stack.paramClasses.remove(forFragment.variable);
        }
    }

    private void buildStaticContent(ContainerFragment parent, CallTypeStack stack, TemplateSource source) {
        if (startTagClosed) parent.addStaticContent("/>");
        else parent.addStaticContent(">");

        for (Node node : nodes) {
            node.buildTemplate(parent, stack, source);
        }

        if (hasEndTag) addEndTag(parent);
    }

    private void addEndTag(ContainerFragment parent) {
        parent.addStaticContent("</");
        parent.addStaticContent(name);
        parent.addStaticContent(">");
    }
}
