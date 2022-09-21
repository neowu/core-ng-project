package core.framework.internal.validate;

import core.framework.internal.reflect.Fields;
import core.framework.internal.reflect.GenericTypes;
import core.framework.util.Sets;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Set;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public class ClassValidator {
    public final Class<?> instanceClass;
    private final Set<Class<?>> visitedClasses = Sets.newHashSet();
    private final ClassValidatorSupport support = new ClassValidatorSupport();

    public Set<Class<?>> allowedValueClasses = Set.of();
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

        support.validateClass(objectClass);
        if (visitor != null) visitor.visitClass(objectClass, path);

        for (Field field : support.declaredFields(objectClass)) {
            if (visitor != null) visitor.visitField(field, path);

            String fieldPath = path(path, field.getName());
            Type fieldType = field.getGenericType();
            if (GenericTypes.isList(fieldType)) {
                visitList(fieldType, field, fieldPath);
            } else if (GenericTypes.isMap(fieldType)) {
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
