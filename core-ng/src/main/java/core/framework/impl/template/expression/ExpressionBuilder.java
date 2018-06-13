package core.framework.impl.template.expression;

import core.framework.impl.asm.CodeBuilder;
import core.framework.impl.asm.DynamicInstanceBuilder;
import core.framework.impl.reflect.GenericTypes;
import core.framework.impl.template.TemplateContext;
import core.framework.impl.template.TemplateMetaContext;
import core.framework.util.Exceptions;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import static core.framework.impl.asm.Literal.type;

/**
 * @author neo
 */
public class ExpressionBuilder {
    private final String expressionSource;
    private final TemplateMetaContext context;
    private final String location;
    private final Token token;

    public ExpressionBuilder(String expressionSource, TemplateMetaContext context, String location) {
        this.expressionSource = expressionSource;
        this.context = context;
        this.location = location;
        token = new ExpressionParser().parse(expressionSource);
    }

    public ExpressionHolder build() {
        Expression expression = buildExpression();
        Type returnType = returnType(token, context.rootClass);
        return new ExpressionHolder(expression, returnType, expressionSource, location);
    }

    private Expression buildExpression() {
        try {
            DynamicInstanceBuilder<Expression> builder = new DynamicInstanceBuilder<>(Expression.class, Expression.class.getCanonicalName());
            builder.addMethod(buildEval());
            return builder.build();
        } catch (Throwable e) {
            throw Exceptions.error("failed to compile expression, expression={}, location={}", expressionSource, location, e);
        }
    }

    private String buildEval() {
        CodeBuilder builder = new CodeBuilder();
        builder.append("public Object eval({} context) {\n", type(TemplateContext.class));
        String rootClassLiteral = type(context.rootClass);
        builder.indent(1).append("{} $root = ({})context.root;\n", rootClassLiteral, rootClassLiteral);
        context.paramClasses.forEach((name, paramClass) -> {
            String paramClassLiteral = type(paramClass);
            builder.indent(1).append("{} {} = ({})context.context(\"{}\");\n", paramClassLiteral, name, paramClassLiteral, name);
        });
        String translatedExpression = new ExpressionTranslator(token, context).translate();
        builder.indent(1).append("return {};\n", translatedExpression);

        builder.append("}");
        return builder.build();
    }

    private Type returnType(Token token, Class<?> modelClass) {
        if (token instanceof MethodToken) {
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
        Class<?> fieldClass = context.paramClasses.get(fieldName);
        if (fieldClass != null) return fieldClass;
        try {
            return modelClass.getField(fieldName).getGenericType();
        } catch (NoSuchFieldException e) {
            throw Exceptions.error("can not find field, class={}, field={}, expression={}, location={}",
                    modelClass, fieldName, expressionSource, location, e);
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
