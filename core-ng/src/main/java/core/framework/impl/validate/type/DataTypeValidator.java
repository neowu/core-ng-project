package core.framework.impl.validate.type;

import core.framework.impl.reflect.Fields;
import core.framework.impl.reflect.GenericTypes;
import core.framework.util.Exceptions;
import core.framework.util.Sets;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.function.Function;

/**
 * @author neo
 */
public class DataTypeValidator {
    public final Type type;
    private final Set<Class<?>> visitedClasses = Sets.newHashSet();
    public boolean allowTopLevelList;
    public boolean allowTopLevelValue;
    public boolean allowTopLevelOptional;
    public boolean allowChild;
    public Function<Class<?>, Boolean> allowedValueClass;
    public TypeVisitor visitor;

    public DataTypeValidator(Type type) {
        this.type = type;
    }

    public void validate() {
        Type type = extractTopLevelOptional(this.type);

        if (GenericTypes.isList(type)) {
            if (!allowTopLevelList) throw Exceptions.error("top level list is not allowed, type={}", type.getTypeName());
            visitList(type, null, null);
        } else {
            if (allowTopLevelValue && allowedValueClass.apply(GenericTypes.rawClass(type))) return;
            visitObject(GenericTypes.rawClass(type), null, null);
        }
    }

    private Type extractTopLevelOptional(Type type) {
        if (GenericTypes.isOptional(type)) {
            if (!allowTopLevelOptional) throw Exceptions.error("top level optional is not allowed, type={}", type.getTypeName());
            if (!GenericTypes.isGenericOptional(type))
                throw Exceptions.error("top level optional must be Optional<T> and T must be class");
            return GenericTypes.optionalValueClass(type);
        }
        return type;
    }

    private void visitObject(Class<?> objectClass, Field owner, String path) {
        if (visitedClasses.contains(objectClass)) {
            throw Exceptions.error("class must not have circular reference, field={}", Fields.path(owner));
        }

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
                if (!GenericTypes.isGenericStringMap(fieldType)) {
                    throw Exceptions.error("map must be Map<String,T> and T must be class, type={}, field={}", type.getTypeName(), Fields.path(field));
                }
                visitValue(GenericTypes.mapValueClass(fieldType), field, fieldPath);
            } else {
                visitValue(GenericTypes.rawClass(fieldType), field, fieldPath);
            }
        }

        visitedClasses.remove(objectClass);
    }

    private void visitValue(Class<?> valueClass, Field owner, String path) {
        if (Date.class.isAssignableFrom(valueClass))
            throw Exceptions.error("java.util.Date is not supported, please use java.time.LocalDateTime instead, field={}", Fields.path(owner));

        if (valueClass.isPrimitive()) {
            throw Exceptions.error("primitive class is not supported, please use object type, class={}, field={}", valueClass.getCanonicalName(), Fields.path(owner));
        }

        if (allowedValueClass.apply(valueClass)) return;

        if (valueClass.getPackage() != null && valueClass.getPackage().getName().startsWith("java")) {
            throw Exceptions.error("field class is not supported, please contract arch team, class={}, field={}", valueClass.getCanonicalName(), Fields.path(owner));
        }

        if (owner != null && !allowChild) {
            throw Exceptions.error("child object is not allowed, class={}, field={}", valueClass.getCanonicalName(), Fields.path(owner));
        }

        visitObject(valueClass, owner, path);
    }

    private void visitList(Type listType, Field owner, String path) {
        if (!GenericTypes.isGenericList(listType)) {
            throw Exceptions.error("list must be List<T> and T must be class, type={}", listType.getTypeName());
        }
        Class<?> valueClass = GenericTypes.listValueClass(listType);
        visitValue(valueClass, owner, path);
    }

    private void validateClass(Class<?> objectClass) {
        if (objectClass.isMemberClass() && !Modifier.isStatic(objectClass.getModifiers()))
            throw Exceptions.error("class must be static, class={}", objectClass.getCanonicalName());

        if (objectClass.isInterface() || Modifier.isAbstract(objectClass.getModifiers()) || !Modifier.isPublic(objectClass.getModifiers()))
            throw Exceptions.error("class must be public concrete, class={}", objectClass.getCanonicalName());
        if (!Object.class.equals(objectClass.getSuperclass())) {
            throw Exceptions.error("class must not have super class, class={}", objectClass.getCanonicalName());
        }
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
