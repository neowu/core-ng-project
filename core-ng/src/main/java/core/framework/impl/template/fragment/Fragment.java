package core.framework.impl.template.fragment;

import core.framework.impl.template.CallStack;

/**
 * @author neo
 */
public interface Fragment {
    void process(StringBuilder builder, CallStack stack);
}
