package core.framework.impl.template.node;

import core.framework.impl.template.expression.CallTypeStack;
import core.framework.impl.template.fragment.ContainerFragment;
import core.framework.impl.template.source.TemplateSource;

/**
 * @author neo
 */
public class Text implements Node {
    private final String content;

    public Text(String content) {
        this.content = content;
    }

    @Override
    public void buildTemplate(ContainerFragment parent, CallTypeStack stack, TemplateSource source) {
        parent.addStaticContent(content);
    }
}
