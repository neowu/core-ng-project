package core.framework.impl.validate.type;

import core.framework.impl.reflect.Fields;
import core.framework.impl.reflect.GenericTypes;
import core.framework.util.Sets;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public class DataTypeValidator {
    public final Type type;
    private final Set<Class<?>> visitedClasses = Sets.newHashSet();
    public Set<Class<?>> allowedValueClasses = Set.of(String.class, Boolean.class,
            Integer.class, Long.class, Double.class, BigDecimal.class,
            LocalDate.class, LocalDateTime.class, ZonedDateTime.class);
    public boolean allowTopLevelList;
    public boolean allowTopLevelValue;
    public boolean allowChild;
    public TypeVisitor visitor;

    public DataTypeValidator(Type type) {
        this.type = type;
    }

    public void validate() {
        if (GenericTypes.isList(type)) {
            if (!allowTopLevelList) throw new Error(format("top level list is not allowed, type={}", type.getTypeName()));
            visitList(type, null, null);
        } else {
            if (allowTopLevelValue && allowedValueClasses.contains(GenericTypes.rawClass(type))) return;
            visitObject(GenericTypes.rawClass(type), null, null);
        }
    }

    private void visitObject(Class<?> objectClass, Field owner, String path) {
        if (visitedClasses.contains(objectClass))
            throw new Error(format("class must not have circular reference, field={}", Fields.path(owner)));

        visitedClasses.add(objectClass);

        validateClass(objectClass);
        if (visitor != null) visitor.visitClass(objectClass, path);

        Field[] fields = objectClass.getDeclaredFields();
        for (Field field : fields) {
            if (field.isSynthetic()) continue;  // ignore dynamic/generated field, e.g. jacoco
            if (Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers())) continue;  // ignore all static final field

            validateField(field);
            if (visitor != null) visitor.visitField(field, path);

            String fieldPath = path(path, field.getName());
            Type fieldType = field.getGenericType();
            if (GenericTypes.isList(fieldType)) {
                if (!allowChild)
                    throw new Error(format("list field is not allowed, field={}", Fields.path(field)));
                visitList(fieldType, field, fieldPath);
            } else if (GenericTypes.isMap(fieldType)) {
                if (!allowChild)
                    throw new Error(format("map field is not allowed, field={}", Fields.path(field)));
                if (!GenericTypes.isGenericStringMap(fieldType))
                    throw new Error(format("map must be Map<String,T> and T must be class, type={}, field={}", type.getTypeName(), Fields.path(field)));
                visitValue(GenericTypes.mapValueClass(fieldType), field, fieldPath);
            } else {
                visitValue(GenericTypes.rawClass(fieldType), field, fieldPath);
            }
        }

        visitedClasses.remove(objectClass);
    }

    private void visitValue(Class<?> valueClass, Field owner, String path) {
        if (Date.class.isAssignableFrom(valueClass))
            throw new Error(format("java.util.Date is not supported, please use java.time.LocalDateTime/ZonedDateTime instead, field={}", Fields.path(owner)));

        if (valueClass.isPrimitive())
            throw new Error(format("primitive class is not supported, please use object type, class={}, field={}", valueClass.getCanonicalName(), Fields.path(owner)));

        if (valueClass.isEnum()) {
            if (visitor != null) {
                visitor.visitEnum(valueClass, path);
            }
            return; // enum is allowed value type
        }

        if (allowedValueClasses.contains(valueClass)) return;

        if (valueClass.getPackageName().startsWith("java"))
            throw new Error(format("field class is not supported, class={}, field={}", valueClass.getCanonicalName(), Fields.path(owner)));

        if (owner != null && !allowChild)
            throw new Error(format("child object is not allowed, class={}, field={}", valueClass.getCanonicalName(), Fields.path(owner)));

        visitObject(valueClass, owner, path);
    }

    private void visitList(Type listType, Field owner, String path) {
        if (!GenericTypes.isGenericList(listType))
            throw new Error(format("list must be List<T> and T must be class, type={}", listType.getTypeName()));

        Class<?> valueClass = GenericTypes.listValueClass(listType);
        visitValue(valueClass, owner, path);
    }

    private void validateClass(Class<?> objectClass) {
        if (objectClass.isPrimitive() || objectClass.getPackageName().startsWith("java") || objectClass.isEnum())
            throw new Error(format("class must be bean class, class={}", objectClass.getCanonicalName()));
        if (objectClass.isMemberClass() && !Modifier.isStatic(objectClass.getModifiers()))
            throw new Error(format("class must be static, class={}", objectClass.getCanonicalName()));
        if (objectClass.isInterface() || Modifier.isAbstract(objectClass.getModifiers()) || !Modifier.isPublic(objectClass.getModifiers()))
            throw new Error(format("class must be public concrete, class={}", objectClass.getCanonicalName()));
        if (!Object.class.equals(objectClass.getSuperclass()))
            throw new Error(format("class must not have super class, class={}", objectClass.getCanonicalName()));

        Constructor<?>[] constructors = objectClass.getDeclaredConstructors();
        if (constructors.length > 1 || constructors[0].getParameterCount() > 1 || !Modifier.isPublic(constructors[0].getModifiers())) {
            throw new Error(format("class must have only one public default constructor, class={}, constructors={}", objectClass.getCanonicalName(), Arrays.toString(constructors)));
        }
    }

    private void validateField(Field field) {
        int modifiers = field.getModifiers();
        if (!Modifier.isPublic(modifiers))
            throw new Error(format("field must be public, field={}", Fields.path(field)));
        if (Modifier.isTransient(modifiers))
            throw new Error(format("field must not be transient, field={}", Fields.path(field)));
        if (Modifier.isStatic(modifiers))
            throw new Error(format("field must not be static, field={}", Fields.path(field)));
        if (Modifier.isFinal(modifiers))
            throw new Error(format("field must not be final, field={}", Fields.path(field)));
    }

    private String path(String parent, String field) {
        if (parent == null) return field;
        return parent + "/" + field;
    }
}
