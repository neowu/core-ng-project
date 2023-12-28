package core.framework.internal.template;

import core.framework.internal.reflect.Methods;
import core.framework.internal.validate.ClassValidator;
import core.framework.internal.validate.ClassVisitor;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Set;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
final class ModelClassValidator implements ClassVisitor {
    private final ClassValidator validator;

    ModelClassValidator(Class<?> modelClass) {
        validator = new ClassValidator(modelClass);
        validator.allowedValueClasses = Set.of(String.class, Boolean.class,
            Integer.class, Long.class, Double.class, BigDecimal.class,
            LocalDate.class, LocalDateTime.class, ZonedDateTime.class);
        validator.visitor = this;
    }

    void validate() {
        validator.validate();
    }

    @Override
    public void visitClass(Class<?> objectClass, String path) {
        Method[] methods = objectClass.getDeclaredMethods();
        for (Method method : methods) {
            if (Modifier.isPublic(method.getModifiers()) && method.getReturnType().isPrimitive()) {
                throw new Error(format("primitive class as return type is not supported, please use object type, returnType={}, method={}", method.getReturnType(), Methods.path(method)));
            }
        }
    }
}
