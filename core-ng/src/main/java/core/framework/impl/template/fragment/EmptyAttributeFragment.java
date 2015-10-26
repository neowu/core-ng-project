package core.framework.impl.template.fragment;

import core.framework.api.util.Exceptions;
import core.framework.impl.reflect.GenericTypes;
import core.framework.impl.template.CallStack;
import core.framework.impl.template.expression.CallTypeStack;
import core.framework.impl.template.expression.ExpressionBuilder;
import core.framework.impl.template.expression.ExpressionHolder;

/**
 * @author neo
 */
public class EmptyAttributeFragment implements Fragment {
    private final String name;
    private final ExpressionHolder expression;

    public EmptyAttributeFragment(String name, String expression, CallTypeStack stack, String location) {
        this.name = name;
        this.expression = new ExpressionBuilder(expression, stack, location).build();
        if (!Boolean.class.equals(GenericTypes.rawClass(this.expression.returnType)))
            throw Exceptions.error("expression must return Boolean, condition={}, returnType={}, location={}",
                expression, this.expression.returnType.getTypeName(), location);
    }

    @Override
    public void process(StringBuilder builder, CallStack stack) {
        Object result = expression.eval(stack);
        if (Boolean.TRUE.equals(result)) {
            builder.append(' ').append(name);
        }
    }
}
