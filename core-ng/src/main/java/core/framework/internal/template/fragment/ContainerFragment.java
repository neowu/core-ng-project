package core.framework.internal.template.fragment;

import core.framework.internal.template.TemplateContext;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @author neo
 */
public abstract class ContainerFragment implements Fragment {
    private final Deque<Fragment> children = new ArrayDeque<>();

    public void addStaticContent(String content) {
        if (!children.isEmpty()) {
            Fragment lastFragment = children.getLast();
            if (lastFragment instanceof StaticFragment staticFragment) {
                staticFragment.append(content);
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

    protected void processChildren(StringBuilder builder, TemplateContext context) {
        for (Fragment child : children) {
            child.process(builder, context);
        }
    }
}
