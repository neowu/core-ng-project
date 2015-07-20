package core.framework.impl.template;

import core.framework.api.util.Lists;

import java.util.List;

/**
 * @author neo
 */
public class CompositeHandler implements FragmentHandler {
    public final List<FragmentHandler> handlers = Lists.newArrayList();

    @Override
    public void process(StringBuilder builder, CallStack stack) {
        for (FragmentHandler handler : handlers) {
            handler.process(builder, stack);
        }
    }
}
