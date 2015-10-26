package core.framework.impl.template.html.node;

import core.framework.api.util.Lists;

import java.util.List;

/**
 * @author neo
 */
public abstract class ContainerNode implements Node {
    protected final List<Node> nodes = Lists.newArrayList();

    public void add(Node node) {
        this.nodes.add(node);
    }
}
