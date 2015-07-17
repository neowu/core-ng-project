package core.framework.impl.template.expression;

import core.framework.impl.template.CallStack;

/**
 * @author neo
 */
public interface Expression {
    Object eval(CallStack stack);
}
