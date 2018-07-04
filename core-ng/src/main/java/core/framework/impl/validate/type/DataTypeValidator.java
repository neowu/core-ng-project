package core.framework.impl.validate.type;

import core.framework.impl.reflect.Fields;
import core.framework.impl.reflect.GenericTypes;
import core.framework.util.Exceptions;
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
            if (!allowTopLevelList) throw Exceptions.error("top level list is not allowed, type={}", type.getTypeName());
            visitList(type, null, null);
        } else {
            if (allowTopLevelValue && allowedValueClasses.contains(GenericTypes.rawClass(type))) return;
            visitObject(GenericTypes.rawClass(type), null, null);
        }
    }

    private void visitObject(Class<?> objectClass, Field owner, String path) {
        if (visitedClasses.contains(objectClass))
            throw Exceptions.error("class must not have circular reference, field={}", Fields.path(owner));

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
                    throw Exceptions.error("list field is not allowed, field={}", Fields.path(field));
                visitList(fieldType, field, fieldPath);
            } else if (GenericTypes.isMap(fieldType)) {
                if (!allowChild)
                    throw Exceptions.error("map field is not allowed, field={}", Fields.path(field));
                if (!GenericTypes.isGenericStringMap(fieldType))
                    throw Exceptions.error("map must be Map<String,T> and T must be class, type={}, field={}", type.getTypeName(), Fields.path(field));
                visitValue(GenericTypes.mapValueClass(fieldType), field, fieldPath);
            } else {
                visitValue(GenericTypes.rawClass(fieldType), field, fieldPath);
            }
        }

        visitedClasses.remove(objectClass);
    }

    private void visitValue(Class<?> valueClass, Field owner, String path) {
        if (Date.class.isAssignableFrom(valueClass))
            throw Exceptions.error("java.util.Date is not supported, please use java.time.LocalDateTime/ZonedDateTime instead, field={}", Fields.path(owner));

        if (valueClass.isPrimitive())
            throw Exceptions.error("primitive class is not supported, please use object type, class={}, field={}", valueClass.getCanonicalName(), Fields.path(owner));

        if (valueClass.isEnum()) {
            if (visitor != null) {
                visitor.visitEnum(valueClass, path);
            }
            return; // enum is allowed value type
        }

        if (allowedValueClasses.contains(valueClass)) return;

        if (valueClass.getPackageName().startsWith("java"))
            throw Exceptions.error("field class is not supported, please use value types such as String/Boolean/Integer/Enum/LocalDateTime, class={}, field={}", valueClass.getCanonicalName(), Fields.path(owner));

        if (owner != null && !allowChild)
            throw Exceptions.error("child object is not allowed, class={}, field={}", valueClass.getCanonicalName(), Fields.path(owner));

        visitObject(valueClass, owner, path);
    }

    private void visitList(Type listType, Field owner, String path) {
        if (!GenericTypes.isGenericList(listType))
            throw Exceptions.error("list must be List<T> and T must be class, type={}", listType.getTypeName());

        Class<?> valueClass = GenericTypes.listValueClass(listType);
        visitValue(valueClass, owner, path);
    }

    private void validateClass(Class<?> objectClass) {
        if (objectClass.isPrimitive() || objectClass.getPackageName().startsWith("java") || objectClass.isEnum())
            throw Exceptions.error("class must be bean class, class={}", objectClass.getCanonicalName());
        if (objectClass.isMemberClass() && !Modifier.isStatic(objectClass.getModifiers()))
            throw Exceptions.error("class must be static, class={}", objectClass.getCanonicalName());
        if (objectClass.isInterface() || Modifier.isAbstract(objectClass.getModifiers()) || !Modifier.isPublic(objectClass.getModifiers()))
            throw Exceptions.error("class must be public concrete, class={}", objectClass.getCanonicalName());
        if (!Object.class.equals(objectClass.getSuperclass()))
            throw Exceptions.error("class must not have super class, class={}", objectClass.getCanonicalName());

        Constructor<?>[] constructors = objectClass.getDeclaredConstructors();
        if (constructors.length > 1 || constructors[0].getParameterCount() > 1 || !Modifier.isPublic(constructors[0].getModifiers())) {
            throw Exceptions.error("class must have only one public default constructor, class={}, constructors={}", objectClass.getCanonicalName(), Arrays.toString(constructors));
        }
    }

    private void validateField(Field field) {
        int modifiers = field.getModifiers();
        if (!Modifier.isPublic(modifiers))
            throw Exceptions.error("field must be public, field={}", Fields.path(field));
        if (Modifier.isTransient(modifiers))
            throw Exceptions.error("field must not be transient, field={}", Fields.path(field));
        if (Modifier.isStatic(modifiers))
            throw Exceptions.error("field must not be static, field={}", Fields.path(field));
        if (Modifier.isFinal(modifiers))
            throw Exceptions.error("field must not be final, field={}", Fields.path(field));
    }

    private String path(String parent, String field) {
        if (parent == null) return field;
        return parent + "/" + field;
    }
}
