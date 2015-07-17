package core.framework.impl.template.expression;

import core.framework.impl.codegen.CodeBuilder;
import core.framework.impl.codegen.CodeCompileException;
import core.framework.impl.codegen.DynamicInstanceBuilder;
import core.framework.impl.template.CallStack;

/**
 * @author neo
 */
public class ExpressionImpl implements Expression {
    public final String expression;
    public final String reference;
    private final Expression delegate;

    public ExpressionImpl(String expression, CallTypeStack stack, String reference) {
        this.expression = expression;
        this.reference = reference;
        DynamicInstanceBuilder<Expression> builder = new DynamicInstanceBuilder<>(Expression.class, Expression.class.getCanonicalName());
        try {
            builder.addMethod(buildEval(expression, stack));
        } catch (CodeCompileException e) {
            throw new Error("failed to compile expression, expression=" + expression + "ref=" + reference, e);
        }
        delegate = builder.build();
    }

    private String buildEval(String expression, CallTypeStack stack) {
        CodeBuilder builder = new CodeBuilder();
        builder.append("public Object eval({} stack) {\n", CallStack.class.getCanonicalName());
        builder.indent(1).append("{} root = ({})stack.root;\n", stack.rootClass.getCanonicalName(), stack.rootClass.getCanonicalName());
        stack.paramClasses
            .forEach((name, paramClass) -> builder.indent(1).append("{} {} = ({})stack.context(\"{}\");\n", paramClass.getCanonicalName(), name, paramClass.getCanonicalName(), name));

        String translatedExpression = new ExpressionTranslator(expression, stack).translate();
        builder.indent(1).append("return {}.result({});\n", ResultHelper.class.getCanonicalName(), translatedExpression);

        builder.append("}");
        return builder.toString();
    }

    @Override
    public Object eval(CallStack stack) {
        try {
            Object result = delegate.eval(stack);
            if (result instanceof HTMLText) return ((HTMLText) result).html;
            else if (result instanceof String) return result;   //TODO: escape html
            else return result;
        } catch (Throwable e) {
            throw new Error("failed to eval expression, expression=" + expression + "ref=" + reference, e);
        }
    }
}
