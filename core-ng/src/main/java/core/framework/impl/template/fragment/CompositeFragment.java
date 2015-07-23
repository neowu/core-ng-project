package core.framework.impl.template.fragment;

import core.framework.api.util.Lists;

import java.util.List;

/**
 * @author neo
 */
public abstract class CompositeFragment implements Fragment {
    public final List<Fragment> handlers = Lists.newArrayList();
}
