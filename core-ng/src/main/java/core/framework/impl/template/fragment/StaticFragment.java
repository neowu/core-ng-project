package core.framework.impl.template.fragment;

import core.framework.impl.template.CallStack;

/**
 * @author neo
 */
public class StaticFragment implements Fragment {
    private final String content;

    public StaticFragment(String content) {
        this.content = content;
    }

    @Override
    public void process(StringBuilder builder, CallStack stack) {
        builder.append(content);
    }
}
