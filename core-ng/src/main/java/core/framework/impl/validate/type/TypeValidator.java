package core.framework.impl.validate.type;

import core.framework.api.util.Exceptions;
import core.framework.api.util.Sets;
import core.framework.impl.reflect.Fields;
import core.framework.impl.reflect.GenericTypes;

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
public class TypeValidator {
    public final Type type;
    public boolean allowTopLevelList;
    public boolean allowTopLevelValue;
    public boolean allowChildObject;
    public boolean allowChildListAndMap;
    public Function<Class, Boolean> allowedValueClass;
    public TypeVisitor visitor;

    private final Set<Class<?>> visitedClasses = Sets.newHashSet();

    public TypeValidator(Type type) {
        this.type = type;
    }

    public void validate() {
        if (GenericTypes.isList(type)) {
            if (!allowTopLevelList)
                throw Exceptions.error("top level list is not allowed, type={}", type.getTypeName());
            visitList(type, null);
        } else {
            if (allowTopLevelValue && allowedValueClass.apply(GenericTypes.rawClass(type))) return;
            visitObject(GenericTypes.rawClass(type), true);
        }
    }

    private void visitObject(Class<?> objectClass, boolean topLevel) {
        if (visitedClasses.contains(objectClass)) {
            return;
        } else {
            visitedClasses.add(objectClass);
        }

        if (objectClass.isInterface() || Modifier.isAbstract(objectClass.getModifiers()) || !Modifier.isPublic(objectClass.getModifiers()))
            throw Exceptions.error("type must be public concrete class, class={}", objectClass.getCanonicalName());
        if (!Object.class.equals(objectClass.getSuperclass())) {
            throw Exceptions.error("class must not inherit from other class, type={}", objectClass.getCanonicalName());
        }
        Constructor[] constructors = objectClass.getDeclaredConstructors();
        if (constructors.length > 1 || constructors[0].getParameterCount() > 1 || !Modifier.isPublic(constructors[0].getModifiers())) {
            throw Exceptions.error("class must contain only one public default constructor, constructors={}", Arrays.toString(constructors));
        }

        if (visitor != null) visitor.visitClass(objectClass, topLevel);
        Field[] fields = objectClass.getDeclaredFields();
        for (Field field : fields) {
            if (visitor != null) visitor.visitField(field, topLevel);
            Type fieldType = field.getGenericType();
            if (GenericTypes.isList(fieldType)) {
                if (!allowChildListAndMap)
                    throw Exceptions.error("list is not allowed as field, field={}", Fields.path(field));
                visitList(fieldType, field);
            } else if (GenericTypes.isMap(fieldType)) {
                if (!allowChildListAndMap)
                    throw Exceptions.error("map is not allowed as field, field={}", Fields.path(field));
                if (!GenericTypes.isGenericStringMap(fieldType)) {
                    throw Exceptions.error("map must be Map<String,T> and T must be class, type={}, field={}", type.getTypeName(), Fields.path(field));
                }
                visitValue(GenericTypes.mapValueClass(fieldType), field);
            } else {
                visitValue(GenericTypes.rawClass(fieldType), field);
            }
        }
    }

    private void visitValue(Class<?> valueClass, Field field) {
        if (field != null) {
            if (!Modifier.isPublic(field.getModifiers()))
                throw Exceptions.error("field must be public, field={}", Fields.path(field));

            if (Modifier.isTransient(field.getModifiers()))
                throw Exceptions.error("field must not be transient, field={}", Fields.path(field));

            if (Modifier.isFinal(field.getModifiers())) {
                throw Exceptions.error("field must not be final, field={}", Fields.path(field));
            }
        }

        if (Date.class.isAssignableFrom(valueClass))
            throw Exceptions.error("java.util.Date is not supported, please use java.time.LocalDateTime instead, field={}", Fields.path(field));

        if (valueClass.isPrimitive()) {
            throw Exceptions.error("primitive class is not supported, please use object type, class={}, field={}", valueClass.getCanonicalName(), Fields.path(field));
        }

        if (allowedValueClass.apply(valueClass)) return;

        if (valueClass.getPackage().getName().startsWith("java")) {
            throw Exceptions.error("field class is not supported, please contract arch team, class={}, field={}", valueClass.getCanonicalName(), Fields.path(field));
        }

        if (field != null && !allowChildObject) {
            throw Exceptions.error("child object is not allowed, class={}, field={}", valueClass.getCanonicalName(), Fields.path(field));
        }
        visitObject(valueClass, false);
    }

    private void visitList(Type listType, Field field) {
        if (!GenericTypes.isGenericList(listType)) {
            throw Exceptions.error("list must be as List<T> and T must be class, type={}", listType.getTypeName());
        }
        Class<?> valueClass = GenericTypes.listValueClass(listType);
        visitValue(valueClass, field);
    }
}
