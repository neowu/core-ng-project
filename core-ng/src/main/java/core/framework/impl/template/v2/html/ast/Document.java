package core.framework.impl.template.v2.html.ast;

import core.framework.api.util.Lists;

import java.util.List;

/**
 * @author neo
 */
public class Document implements Node {
    public final List<Node> nodes = Lists.newArrayList();

    public String content() {
        StringBuilder builder = new StringBuilder();
        print(builder);
        return builder.toString();
    }

    @Override
    public void print(StringBuilder builder) {
        for (Node node : nodes) {
            node.print(builder);
        }
    }
}
