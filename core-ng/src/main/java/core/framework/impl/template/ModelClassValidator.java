package core.framework.impl.template;

import core.framework.impl.validate.type.DataTypeValidator;
import core.framework.impl.validate.type.TypeVisitor;
import core.framework.util.Exceptions;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

/**
 * @author neo
 */
class ModelClassValidator implements TypeVisitor {
    private final DataTypeValidator validator;

    ModelClassValidator(Class<?> modelClass) {
        validator = new DataTypeValidator(modelClass);
        validator.allowedValueClass = this::allowedValueClass;
        validator.allowChild = true;
        validator.visitor = this;
    }

    void validate() {
        validator.validate();
    }

    private boolean allowedValueClass(Class<?> valueClass) {
        return String.class.equals(valueClass)
                || Integer.class.equals(valueClass)
                || Boolean.class.equals(valueClass)
                || Long.class.equals(valueClass)
                || Double.class.equals(valueClass)
                || BigDecimal.class.equals(valueClass)
                || LocalDate.class.equals(valueClass)
                || LocalDateTime.class.equals(valueClass)
                || ZonedDateTime.class.equals(valueClass)
                || valueClass.isEnum()
                || "org.bson.types.ObjectId".equals(valueClass.getCanonicalName()); // not depends on mongo jar if application doesn't include mongo driver;
    }

    @Override
    public void visitClass(Class<?> objectClass, String path) {
        Method[] methods = objectClass.getDeclaredMethods();
        for (Method method : methods) {
            if (Modifier.isPublic(method.getModifiers()) && method.getReturnType().isPrimitive()) {
                throw Exceptions.error("primitive class as return type is not supported, please use object type, returnType={}, method={}", method.getReturnType(), method.getDeclaringClass().getCanonicalName() + "." + method.getName());
            }
        }
    }
}
