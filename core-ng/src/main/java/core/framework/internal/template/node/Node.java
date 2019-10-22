package core.framework.internal.template.node;

import core.framework.internal.template.TemplateMetaContext;
import core.framework.internal.template.fragment.ContainerFragment;
import core.framework.internal.template.source.TemplateSource;

/**
 * @author neo
 */
public interface Node {
    void buildTemplate(ContainerFragment parent, TemplateMetaContext context, TemplateSource source);
}
