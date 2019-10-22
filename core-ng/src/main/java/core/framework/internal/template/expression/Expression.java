package core.framework.internal.template.expression;

import core.framework.internal.template.TemplateContext;

/**
 * @author neo
 */
public interface Expression {
    Object eval(TemplateContext context);
}
