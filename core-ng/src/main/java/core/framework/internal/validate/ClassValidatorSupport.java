package core.framework.internal.validate;

import core.framework.internal.reflect.Fields;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public final class ClassValidatorSupport {
    public void validateClass(Class<?> objectClass) {
        if (objectClass.isPrimitive() || objectClass.getPackageName().startsWith("java") || objectClass.isEnum())
            throw new Error("class must be bean class, class=" + objectClass.getCanonicalName());
        if (objectClass.isMemberClass() && !Modifier.isStatic(objectClass.getModifiers()))
            throw new Error("class must be static, class=" + objectClass.getCanonicalName());
        if (objectClass.isInterface() || Modifier.isAbstract(objectClass.getModifiers()) || !Modifier.isPublic(objectClass.getModifiers()))
            throw new Error("class must be public concrete, class=" + objectClass.getCanonicalName());
        if (!Object.class.equals(objectClass.getSuperclass()))
            throw new Error("class must not have super class, class=" + objectClass.getCanonicalName());

        Constructor<?>[] constructors = objectClass.getDeclaredConstructors();
        if (constructors.length > 1 || constructors[0].getParameterCount() > 0 || !Modifier.isPublic(constructors[0].getModifiers())) {
            throw new Error(format("class must only have public default constructor, class={}, constructors={}", objectClass.getCanonicalName(), Arrays.toString(constructors)));
        }
    }

    // return fields to validate
    @SuppressWarnings("PMD.UseArraysAsList")    // false positive
    public List<Field> declaredFields(Class<?> objectClass) {
        Field[] fields = objectClass.getDeclaredFields();
        List<Field> results = new ArrayList<>(fields.length);
        for (Field field : fields) {
            if (field.isSynthetic()) continue;  // ignore dynamic/generated field, e.g. jacoco
            if (Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers())) continue;  // ignore all static final field

            validateField(field);
            results.add(field);
        }
        return results;
    }

    void validateField(Field field) {
        int modifiers = field.getModifiers();
        if (Modifier.isStatic(modifiers))
            throw new Error("field must not be static, field=" + Fields.path(field));
        if (!Modifier.isPublic(modifiers))
            throw new Error("field must be public, field=" + Fields.path(field));
        if (Modifier.isTransient(modifiers))
            throw new Error("field must not be transient, field=" + Fields.path(field));
        if (Modifier.isFinal(modifiers))
            throw new Error("field must not be final, field=" + Fields.path(field));

        Class<?> fieldClass = field.getType();
        if (Date.class.isAssignableFrom(fieldClass))
            throw new Error("java.util.Date is not supported, please use java.time instead, field=" + Fields.path(field));
        if (fieldClass.isPrimitive())
            throw new Error(format("primitive class is not supported, please use object type, class={}, field={}", fieldClass.getCanonicalName(), Fields.path(field)));
    }
}
