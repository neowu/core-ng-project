package core.framework.impl.template;

import core.framework.api.util.Lists;

import java.util.List;

/**
 * @author neo
 */
public abstract class CompositeHandler implements FragmentHandler {
    public final List<FragmentHandler> handlers = Lists.newArrayList();
}
