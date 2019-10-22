package core.framework.internal.template.node;

import core.framework.internal.template.TemplateMetaContext;
import core.framework.internal.template.fragment.ContainerFragment;
import core.framework.internal.template.source.TemplateSource;

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
