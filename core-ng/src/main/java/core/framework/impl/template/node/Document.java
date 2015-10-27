package core.framework.impl.template.node;

import core.framework.impl.template.TemplateMetaContext;
import core.framework.impl.template.fragment.ContainerFragment;
import core.framework.impl.template.source.TemplateSource;

/**
 * @author neo
 */
public class Document extends ContainerNode {
    @Override
    public void buildTemplate(ContainerFragment parent, TemplateMetaContext context, TemplateSource source) {
        for (Node node : nodes) {
            node.buildTemplate(parent, context, source);
        }
    }
}
