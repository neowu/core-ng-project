package core.framework.impl.template.expression;

import core.framework.impl.codegen.CodeBuilder;
import core.framework.impl.codegen.DynamicInstanceBuilder;
import core.framework.impl.template.CallStack;

/**
 * @author neo
 */
public class ExpressionBuilder {
    public Expression build(Token expression, CallTypeStack stack, Class returnType) {
        DynamicInstanceBuilder<Expression> builder = new DynamicInstanceBuilder<>(Expression.class, Expression.class.getCanonicalName());
        builder.addMethod(buildEval(expression, stack, returnType));
        return builder.build();
    }

    private String buildEval(Token expression, CallTypeStack stack, Class returnType) {
        CodeBuilder builder = new CodeBuilder();
        builder.append("public Object eval({} stack) {\n", CallStack.class.getCanonicalName());
        builder.indent(1).append("{} $root = ({})stack.root;\n", stack.rootClass.getCanonicalName(), stack.rootClass.getCanonicalName());
        stack.paramClasses.forEach((name, paramClass) -> builder.indent(1).append("{} {} = ({})stack.context(\"{}\");\n",
            paramClass.getCanonicalName(), name, paramClass.getCanonicalName(), name));

        String translatedExpression = new ExpressionTranslator(expression, stack).translate();

        if (Boolean.class.equals(returnType)) {
            builder.indent(1).append("return Boolean.valueOf(Boolean.TRUE.equals({}));\n", translatedExpression);
        } else {
            builder.indent(1).append("return {};\n", translatedExpression);
        }

        builder.append("}");
        return builder.toString();
    }
}
