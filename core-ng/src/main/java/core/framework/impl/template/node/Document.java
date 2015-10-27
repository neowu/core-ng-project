package core.framework.impl.template.node;

import core.framework.impl.template.expression.CallTypeStack;
import core.framework.impl.template.fragment.ContainerFragment;
import core.framework.impl.template.source.TemplateSource;

/**
 * @author neo
 */
public class Document extends ContainerNode {
    @Override
    public void buildTemplate(ContainerFragment parent, CallTypeStack stack, TemplateSource source) {
        for (Node node : nodes) {
            node.buildTemplate(parent, stack, source);
        }
    }
}
