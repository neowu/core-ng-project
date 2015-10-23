package core.framework.impl.template.fragment;

import core.framework.api.util.Lists;

import java.util.List;

/**
 * @author neo
 */
public abstract class CompositeFragment implements Fragment {
    public final List<Fragment> fragments = Lists.newArrayList();

    public void addStaticContent(String content) {
        if (!fragments.isEmpty()) {
            Fragment lastFragment = fragments.get(fragments.size() - 1);
            if (lastFragment instanceof StaticFragment) {
                ((StaticFragment) lastFragment).content.append(content);
                return;
            }
        }
        StaticFragment fragment = new StaticFragment();
        fragment.content.append(content);
        fragments.add(fragment);
    }
}
