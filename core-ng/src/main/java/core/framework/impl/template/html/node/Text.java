package core.framework.impl.template.html.node;

import core.framework.impl.template.expression.CallTypeStack;
import core.framework.impl.template.fragment.ContainerFragment;
import core.framework.impl.template.source.TemplateSource;

/**
 * @author neo
 */
public class Text implements Node {
    public final String content;

    public Text(String content) {
        this.content = content;
    }

    @Override
    public void buildTemplate(ContainerFragment fragment, CallTypeStack stack, TemplateSource source) {
        fragment.addStaticContent(content);
    }
}
