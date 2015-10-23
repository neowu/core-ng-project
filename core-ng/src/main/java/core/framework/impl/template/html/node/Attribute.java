package core.framework.impl.template.html.node;

import core.framework.impl.template.fragment.CompositeFragment;

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

    public void addStaticContent(CompositeFragment fragment) {
        fragment.addStaticContent(" ");
        fragment.addStaticContent(name);
        if (value != null) {
            fragment.addStaticContent("=");
            if (hasDoubleQuote) fragment.addStaticContent("\"");
            fragment.addStaticContent(value);
            if (hasDoubleQuote) fragment.addStaticContent("\"");
        }
    }
}
