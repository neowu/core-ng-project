package core.framework.internal.template.node;

import core.framework.internal.template.TemplateMetaContext;
import core.framework.internal.template.fragment.ContainerFragment;
import core.framework.internal.template.source.TemplateSource;

/**
 * @author neo
 */
public class Text implements Node {
    public final String content;

    public Text(String content) {
        this.content = content;
    }

    @Override
    public void buildTemplate(ContainerFragment parent, TemplateMetaContext context, TemplateSource source) {
        parent.addStaticContent(content);
    }
}
