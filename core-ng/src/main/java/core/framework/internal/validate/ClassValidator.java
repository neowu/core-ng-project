package core.framework.internal.validate;

import core.framework.internal.reflect.Fields;
import core.framework.internal.reflect.GenericTypes;
import core.framework.util.Sets;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public class ClassValidator {
    public static void validateClass(Class<?> objectClass) {
        if (objectClass.isPrimitive() || objectClass.getPackageName().startsWith("java") || objectClass.isEnum())
            throw new Error("class must be bean class, class=" + objectClass.getCanonicalName());
        if (objectClass.isMemberClass() && !Modifier.isStatic(objectClass.getModifiers()))
            throw new Error("class must be static, class=" + objectClass.getCanonicalName());
        if (objectClass.isInterface() || Modifier.isAbstract(objectClass.getModifiers()) || !Modifier.isPublic(objectClass.getModifiers()))
            throw new Error("class must be public concrete, class=" + objectClass.getCanonicalName());
        if (!Object.class.equals(objectClass.getSuperclass()))
            throw new Error("class must not have super class, class=" + objectClass.getCanonicalName());

        Constructor<?>[] constructors = objectClass.getDeclaredConstructors();
        if (constructors.length > 1 || constructors[0].getParameterCount() > 1 || !Modifier.isPublic(constructors[0].getModifiers())) {
            throw new Error(format("class must have only one public default constructor, class={}, constructors={}", objectClass.getCanonicalName(), Arrays.toString(constructors)));
        }
    }

    public static void validateField(Field field) {
        int modifiers = field.getModifiers();
        if (!Modifier.isPublic(modifiers))
            throw new Error("field must be public, field=" + Fields.path(field));
        if (Modifier.isTransient(modifiers))
            throw new Error("field must not be transient, field=" + Fields.path(field));
        if (Modifier.isStatic(modifiers))
            throw new Error("field must not be static, field=" + Fields.path(field));
        if (Modifier.isFinal(modifiers))
            throw new Error("field must not be final, field=" + Fields.path(field));

        Class<?> fieldClass = field.getType();
        if (Date.class.isAssignableFrom(fieldClass))
            throw new Error("java.util.Date is not supported, please use java.time instead, field=" + Fields.path(field));
        if (fieldClass.isPrimitive())
            throw new Error(format("primitive class is not supported, please use object type, class={}, field={}", fieldClass.getCanonicalName(), Fields.path(field)));
    }

    public final Class<?> instanceClass;
    private final Set<Class<?>> visitedClasses = Sets.newHashSet();
    public Set<Class<?>> allowedValueClasses = Set.of();
    public boolean allowChild;
    public ClassVisitor visitor;

    public ClassValidator(Class<?> instanceClass) {
        this.instanceClass = instanceClass;
    }

    public void validate() {
        visitObject(instanceClass, null, null);
    }

    private void visitObject(Class<?> objectClass, Field owner, String path) {
        if (visitedClasses.contains(objectClass))
            throw new Error("class must not have circular reference, field=" + Fields.path(owner));

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
                if (!allowChild) throw new Error("list field is not allowed, field=" + Fields.path(field));

                visitList(fieldType, field, fieldPath);
            } else if (GenericTypes.isMap(fieldType)) {
                if (!allowChild) throw new Error("map field is not allowed, field=" + Fields.path(field));

                visitMap(fieldType, field, fieldPath);
            } else {
                visitValue(GenericTypes.rawClass(fieldType), field, fieldPath);
            }
        }

        visitedClasses.remove(objectClass);
    }

    private void visitValue(Class<?> valueClass, Field owner, String path) {
        if (valueClass.isEnum()) {
            if (visitor != null) visitor.visitEnum(valueClass);
            return; // enum is allowed value type
        }

        if (allowedValueClasses.contains(valueClass)) return;

        if (valueClass.getPackageName().startsWith("java"))
            throw new Error(format("field class is not supported, class={}, field={}", valueClass.getCanonicalName(), Fields.path(owner)));

        if (owner != null && !allowChild)
            throw new Error(format("child object is not allowed, class={}, field={}", valueClass.getCanonicalName(), Fields.path(owner)));

        visitObject(valueClass, owner, path);
    }

    private void visitMap(Type fieldType, Field owner, String path) {
        if (!GenericTypes.isGenericMap(fieldType))
            throw new Error("map must be Map<K,V>, K must be String or Enum and V must be class or List<Value>, field=" + Fields.path(owner));

        Class<?> keyClass = GenericTypes.mapKeyClass(fieldType);
        if (!String.class.equals(keyClass) && !keyClass.isEnum())
            throw new Error("map key must be String or Enum, field=" + Fields.path(owner));
        if (visitor != null && keyClass.isEnum())
            visitor.visitEnum(keyClass);

        Type mapValueType = GenericTypes.mapValueType(fieldType);
        if (GenericTypes.isList(mapValueType)) {
            if (!GenericTypes.isGenericList(mapValueType))
                throw new Error("map must be Map<K,List<V>> and V must be value class, field=" + Fields.path(owner));

            Class<?> listValueClass = GenericTypes.listValueClass(mapValueType);
            if (!allowedValueClasses.contains(listValueClass)) {
                throw new Error(format("map list value class is not supported, class={}, field={}", listValueClass.getCanonicalName(), Fields.path(owner)));
            }
        } else {
            visitValue(GenericTypes.rawClass(mapValueType), owner, path);
        }
    }

    private void visitList(Type listType, Field owner, String path) {
        if (!GenericTypes.isGenericList(listType))
            throw new Error("list must be List<T> and T must be class, field=" + Fields.path(owner));

        Class<?> valueClass = GenericTypes.listValueClass(listType);
        visitValue(valueClass, owner, path);
    }

    private String path(String parent, String field) {
        if (parent == null) return field;
        return parent + "/" + field;
    }
}
