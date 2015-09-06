package core.framework.impl.template.expression;

import core.framework.api.util.Exceptions;
import core.framework.impl.reflect.TypeInspector;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * @author neo
 */
public class ExpressionTypeInspector {
    public Class<?> listValueClass(Token expression, Class<?> modelClass, String expressionText) {
        if (expression instanceof MethodToken) {
            if (((MethodToken) expression).builtinMethod)
                throw Exceptions.error("list expression must not be builtin method, expression={}", expressionText);
            String methodName = ((MethodToken) expression).name;
            TypeInspector type = new TypeInspector(methodReturnType(modelClass, methodName));
            if (((MethodToken) expression).next != null) {
                return listValueClass(((MethodToken) expression).next, type.rawClass, expressionText);
            }
            if (type.isList()) return type.listValueClass();
        }
        if (expression instanceof FieldToken) {
            TypeInspector type = new TypeInspector(fieldType(modelClass, ((FieldToken) expression).name));
            if (((FieldToken) expression).next != null) {
                return listValueClass(((FieldToken) expression).next, type.rawClass, expressionText);
            }
            if (type.isList()) return type.listValueClass();
        }
        throw Exceptions.error("expression must return List<>, expression={}", expressionText);
    }

    private Type fieldType(Class<?> instanceClass, String fieldName) {
        try {
            return instanceClass.getField(fieldName).getGenericType();
        } catch (NoSuchFieldException e) {
            throw new Error(e);
        }
    }

    private Type methodReturnType(Class<?> instanceClass, String methodName) {
        Method[] methods = instanceClass.getMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) return method.getGenericReturnType();
        }
        throw Exceptions.error("can not find method, class={}, methodName={}", instanceClass, methodName);
    }
}
