package core.framework.impl.template.fragment;

import core.framework.api.util.Lists;
import core.framework.impl.template.CallStack;

import java.util.List;

/**
 * @author neo
 */
public abstract class ContainerFragment implements Fragment {
    private final List<Fragment> children = Lists.newArrayList();

    public void addStaticContent(String content) {
        if (!children.isEmpty()) {
            Fragment lastFragment = children.get(children.size() - 1);
            if (lastFragment instanceof StaticFragment) {
                ((StaticFragment) lastFragment).append(content);
                return;
            }
        }
        StaticFragment fragment = new StaticFragment();
        fragment.append(content);
        children.add(fragment);
    }

    public void add(Fragment fragment) {
        children.add(fragment);
    }

    protected void processChildren(StringBuilder builder, CallStack stack) {
        for (Fragment child : children) {
            child.process(builder, stack);
        }
    }
}
