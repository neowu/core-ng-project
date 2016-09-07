package core.framework.impl.template;

import core.framework.api.util.Exceptions;
import core.framework.impl.validate.type.DataTypeValidator;
import core.framework.impl.validate.type.TypeVisitor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

/**
 * @author neo
 */
public class ModelClassValidator implements TypeVisitor {
    private final DataTypeValidator validator;

    public ModelClassValidator(Class<?> modelClass) {
        validator = new DataTypeValidator(modelClass);
        validator.allowedValueClass = this::allowedValueClass;
        validator.allowChild = true;
        validator.allowTopLevelList = false;
        validator.visitor = this;
    }

    public void validate() {
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
            || Instant.class.equals(valueClass)
            || Enum.class.isAssignableFrom(valueClass)
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

    @Override
    public void visitField(Field field, String parentPath) {

    }
}
