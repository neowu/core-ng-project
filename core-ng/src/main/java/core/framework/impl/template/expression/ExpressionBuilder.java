package core.framework.impl.template.expression;

import core.framework.api.util.Strings;
import core.framework.impl.code.CodeBuilder;
import core.framework.impl.code.CodeCompileException;
import core.framework.impl.code.DynamicInstanceBuilder;
import core.framework.impl.template.CallStack;

/**
 * @author neo
 */
public class ExpressionBuilder {
    private final String expressionSource;
    private final CallTypeStack stack;
    private final String location;
    private final Token token;

    public ExpressionBuilder(String expressionSource, CallTypeStack stack, String location) {
        this.expressionSource = expressionSource;
        this.stack = stack;
        this.location = location;
        token = new ExpressionParser().parse(expressionSource);
    }

    public ExpressionWithSourceInfo build() {
        Expression expression = buildExpression();
        return new ExpressionWithSourceInfo(expression, expressionSource, location);
    }

    public Class<?> listValueClass() {
        return new ExpressionTypeInspector().listValueClass(token, stack.rootClass, expressionSource);
    }

    private Expression buildExpression() {
        try {
            DynamicInstanceBuilder<Expression> builder = new DynamicInstanceBuilder<>(Expression.class, Expression.class.getCanonicalName());
            builder.addMethod(buildEval());
            return builder.build();
        } catch (CodeCompileException e) {
            throw new Error(Strings.format("failed to compile expression, expression={}, location={}", expressionSource, location), e);
        }
    }

    private String buildEval() {
        CodeBuilder builder = new CodeBuilder();
        builder.append("public Object eval({} stack) {\n", CallStack.class.getCanonicalName());
        builder.indent(1).append("{} $root = ({})stack.root;\n", stack.rootClass.getCanonicalName(), stack.rootClass.getCanonicalName());
        stack.paramClasses.forEach((name, paramClass) -> builder.indent(1).append("{} {} = ({})stack.context(\"{}\");\n",
            paramClass.getCanonicalName(), name, paramClass.getCanonicalName(), name));

        String translatedExpression = new ExpressionTranslator(token, stack).translate();
        builder.indent(1).append("return {};\n", translatedExpression);

        builder.append("}");
        return builder.build();
    }
}
