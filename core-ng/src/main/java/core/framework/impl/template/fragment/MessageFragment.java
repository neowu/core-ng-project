package core.framework.impl.template.fragment;

import core.framework.impl.template.TemplateContext;

/**
 * @author neo
 */
public class MessageFragment implements Fragment {
    private final String key;

    public MessageFragment(String key) {
        this.key = key;
    }

    @Override
    public void process(StringBuilder builder, TemplateContext context) {
        builder.append(context.message.message(key));
    }
}
