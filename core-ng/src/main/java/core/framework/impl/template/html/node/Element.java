package core.framework.impl.template.html.node;

import core.framework.api.util.Lists;
import core.framework.impl.template.expression.CallTypeStack;
import core.framework.impl.template.fragment.CompositeFragment;
import core.framework.impl.template.fragment.ForFragment;
import core.framework.impl.template.fragment.HTMLContentFragment;
import core.framework.impl.template.fragment.IfFragment;
import core.framework.impl.template.fragment.MessageFragment;
import core.framework.impl.template.fragment.TextContentFragment;
import core.framework.impl.template.html.HTMLParser;
import core.framework.impl.template.source.TemplateSource;

import java.util.List;

/**
 * @author neo
 */
public class Element implements Node {
    public final List<Node> nodes = Lists.newArrayList();
    public final Attributes attributes = new Attributes();
    public final String name;
    public boolean startTagClosed;
    public boolean hasCloseTag;

    public Element(String name) {
        this.name = name;
    }

    @Override
    public void buildTemplate(CompositeFragment fragment, CallTypeStack stack, TemplateSource source) {
        CompositeFragment root = fragment;
        ForFragment forFragment = null;
        for (Attribute attribute : attributes.flowAttributes()) {
            if ("c:if".equals(attribute.name)) {
                IfFragment ifFragment = new IfFragment(attribute.value, stack, attribute.location);
                root.fragments.add(ifFragment);
                root = ifFragment;
            } else if ("c:for".equals(attribute.name)) {
                forFragment = new ForFragment(attribute.value, stack, attribute.location);
                stack.paramClasses.put(forFragment.variable, forFragment.valueClass);
                root.fragments.add(forFragment);
                root = forFragment;
            }
        }

        root.addStaticContent("<");
        root.addStaticContent(name);

        attributes.buildTemplate(fragment, stack);

        if (attributes.isContentDynamic()) {
            root.addStaticContent(">");

            Attribute attribute = attributes.dynamicContentAttribute();
            String name = attribute.name;
            if ("c:text".equals(name)) {
                root.fragments.add(new TextContentFragment(attribute.value, stack, attribute.location));
            } else if ("c:html".equals(name)) {
                root.fragments.add(new HTMLContentFragment(attribute.value, stack, attribute.location));
            } else if ("c:msg".equals(name)) {
                root.fragments.add(new MessageFragment(attribute.value, stack, attribute.location));
            } else if ("c:include".equals(name)) {
                TemplateSource includedSource = source.resolve(attribute.value);
                Document document = new HTMLParser(includedSource).parse();
                document.buildTemplate(root, stack, includedSource);
            }

            root.addStaticContent("</");
            root.addStaticContent(this.name);
            root.addStaticContent(">");
        } else {
            buildStaticContent(root, stack, source);
        }

        if (forFragment != null) {
            stack.paramClasses.remove(forFragment.variable);
        }
    }

    private void buildStaticContent(CompositeFragment root, CallTypeStack stack, TemplateSource source) {
        if (startTagClosed) root.addStaticContent("/>");
        else root.addStaticContent(">");


        for (Node node : nodes) {
            node.buildTemplate(root, stack, source);
        }

        if (hasCloseTag) {
            root.addStaticContent("</");
            root.addStaticContent(name);
            root.addStaticContent(">");
        }
    }
}
