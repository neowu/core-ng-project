package core.framework.impl.template.html.node;

import core.framework.api.util.Lists;
import core.framework.impl.template.expression.CallTypeStack;
import core.framework.impl.template.fragment.CompositeFragment;
import core.framework.impl.template.source.TemplateSource;

import java.util.List;

/**
 * @author neo
 */
public class Document implements Node {
    public final List<Node> nodes = Lists.newArrayList();

    @Override
    public void buildTemplate(CompositeFragment fragment, CallTypeStack stack, TemplateSource source) {
        for (Node node : nodes) {
            node.buildTemplate(fragment, stack, source);
        }
    }
}
