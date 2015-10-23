package core.framework.impl.template.fragment;

import core.framework.impl.template.CallStack;

/**
 * @author neo
 */
public class StaticFragment implements Fragment {
    final StringBuilder content = new StringBuilder();

    @Override
    public void process(StringBuilder builder, CallStack stack) {
        builder.append(content);
    }
}
