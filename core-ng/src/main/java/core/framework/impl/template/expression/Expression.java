package core.framework.impl.template.expression;

import core.framework.impl.template.TemplateContext;

/**
 * @author neo
 */
public interface Expression {
    Object eval(TemplateContext context);
}
