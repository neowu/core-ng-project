package core.framework.impl.template.v2.html.ast;

import core.framework.api.util.Lists;

import java.util.List;

/**
 * @author neo
 */
public class Element implements Node {
    public final List<Node> nodes = Lists.newArrayList();
    public final List<Attribute> attributes = Lists.newArrayList();
    public final String name;
    public boolean startTagClosed;
    public boolean hasCloseTag;

    public Element(String name) {
        this.name = name;
    }

    @Override
    public void print(StringBuilder builder) {
        builder.append("<").append(name);
        for (Attribute attribute : attributes) {
            builder.append(" ");
            attribute.print(builder);
        }
        if (startTagClosed) builder.append("/>");
        else builder.append(">");

        for (Node node : nodes) {
            node.print(builder);
        }

        if (hasCloseTag) {
            builder.append("</").append(name).append(">");
        }
    }
}
