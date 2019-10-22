package core.framework.internal.template.node;

import core.framework.util.Lists;

import java.util.List;

/**
 * @author neo
 */
public abstract class ContainerNode implements Node {
    public final List<Node> nodes = Lists.newArrayList();

    public void add(Node node) {
        this.nodes.add(node);
    }
}
