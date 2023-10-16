package core.framework.internal.template.fragment;

import core.framework.internal.template.TemplateContext;

/**
 * @author neo
 */
public interface Fragment {
    void process(StringBuilder builder, TemplateContext context);
}
