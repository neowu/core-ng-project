package core.framework.impl.template.expression;

import core.framework.api.util.Exceptions;
import core.framework.api.util.Strings;
import core.framework.impl.code.CodeBuilder;
import core.framework.impl.code.CodeCompileException;
import core.framework.impl.code.DynamicInstanceBuilder;
import core.framework.impl.reflect.GenericTypes;
import core.framework.impl.template.CallStack;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

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

    public ExpressionHolder build() {
        Expression expression = buildExpression();
        Type returnType = returnType(token, stack.rootClass);
        return new ExpressionHolder(expression, returnType, expressionSource, location);
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

    private Type returnType(Token token, Class<?> modelClass) {
        if (token instanceof MethodToken) {
            if (((MethodToken) token).builtinMethod)
                return Object.class;
            String methodName = ((MethodToken) token).name;
            Type returnType = methodReturnType(modelClass, methodName);
            if (((MethodToken) token).next != null) {
                return returnType(((MethodToken) token).next, GenericTypes.rawClass(returnType));
            }
            return returnType;
        } else if (token instanceof FieldToken) {
            Type fieldType = fieldType(modelClass, ((FieldToken) token).name);
            if (((FieldToken) token).next != null) {
                return returnType(((FieldToken) token).next, GenericTypes.rawClass(fieldType));
            }
            return fieldType;
        } else {
            return ((ValueToken) token).type;
        }
    }

    private Type fieldType(Class<?> modelClass, String fieldName) {
        Class<?> fieldClass = stack.paramClasses.get(fieldName);
        if (fieldClass != null) return fieldClass;
        try {
            return modelClass.getField(fieldName).getGenericType();
        } catch (NoSuchFieldException e) {
            throw new Error(Strings.format("can not find field, class={}, field={}, expression={}, location={}",
                modelClass, fieldName, expressionSource, location), e);
        }
    }

    private Type methodReturnType(Class<?> modelClass, String methodName) {
        Method[] methods = modelClass.getMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) return method.getGenericReturnType();
        }
        throw Exceptions.error("can not find method, class={}, method={}, expression={}, location={}",
            modelClass, methodName, expressionSource, location);
    }
}
