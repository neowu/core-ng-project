package core.framework.impl.validate.type;

import core.framework.api.util.Exceptions;
import core.framework.api.util.Sets;
import core.framework.impl.reflect.TypeInspector;

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
    public boolean allowTopLevelList;
    public boolean allowTopLevelValue;
    public boolean allowChildObject;
    public boolean allowChildListAndMap;
    public Function<Class, Boolean> allowedValueClass;
    public TypeVisitor visitor;

    private final Set<Type> visitedTypes = Sets.newHashSet();

    public DataTypeValidator(Type type) {
        this.type = type;
    }

    public void validate() {
        TypeInspector inspector = new TypeInspector(type);
        if (inspector.isList()) {
            if (!allowTopLevelList)
                throw Exceptions.error("top level list is not allowed, type={}", inspector.type.getTypeName());
            visitList(inspector, null);
        } else {
            if (allowTopLevelValue && allowedValueClass.apply(inspector.rawClass)) return;
            visitObjectType(inspector, true);
        }
    }

    private void visitObjectType(TypeInspector objectType, boolean topLevel) {
        if (visitedTypes.contains(objectType.type)) {
            return;
        } else {
            visitedTypes.add(objectType.type);
        }

        if (objectType.rawClass.isInterface() || Modifier.isAbstract(objectType.rawClass.getModifiers()) || !Modifier.isPublic(objectType.rawClass.getModifiers()))
            throw Exceptions.error("type must be public concrete class, type={}", objectType.type.getTypeName());
        if (!Object.class.equals(objectType.rawClass.getSuperclass())) {
            throw Exceptions.error("class must not inherit from other class, type={}", objectType.type.getTypeName());
        }
        Constructor[] constructors = objectType.rawClass.getDeclaredConstructors();
        if (constructors.length > 1 || constructors[0].getParameterCount() > 1 || !Modifier.isPublic(constructors[0].getModifiers())) {
            throw Exceptions.error("class must contain only one public default constructor, constructors={}", Arrays.toString(constructors));
        }

        if (visitor != null) visitor.visitClass(objectType.rawClass, topLevel);
        Field[] fields = objectType.rawClass.getDeclaredFields();
        for (Field field : fields) {
            if (visitor != null) visitor.visitField(field, topLevel);
            TypeInspector fieldType = new TypeInspector(field.getGenericType());
            if (fieldType.isList()) {
                if (!allowChildListAndMap)
                    throw Exceptions.error("list is not allowed as field, type={}, field={}", fieldType.type.getTypeName(), field);
                visitList(fieldType, field);
            } else if (fieldType.isMap()) {
                if (!allowChildListAndMap)
                    throw Exceptions.error("map is not allowed as field, type={}, field={}", fieldType.type.getTypeName(), field);
                if (!fieldType.isGenericStringMap()) {
                    throw Exceptions.error("map must be as Map<String,T> and T must be class type, type={}, field={}", fieldType.type.getTypeName(), field);
                }
                visitValue(new TypeInspector(fieldType.mapValueClass()), field);
            } else {
                visitValue(fieldType, field);
            }
        }
    }

    private void visitValue(TypeInspector valueType, Field field) {
        if (field != null) {
            if (!Modifier.isPublic(field.getModifiers()))
                throw Exceptions.error("field must be public, field={}", field);

            if (Modifier.isTransient(field.getModifiers()))
                throw Exceptions.error("field must not be transient, field={}", field);

            if (Modifier.isFinal(field.getModifiers())) {
                throw Exceptions.error("field must not be final, field={}", field);
            }
        }

        if (Date.class.isAssignableFrom(valueType.rawClass))
            throw Exceptions.error("java.util.Date is not supported, please use java.time.LocalDateTime instead, field={}", field);

        if (valueType.rawClass.isPrimitive()) {
            throw Exceptions.error("primitive class is not supported, please use object type, type={}, field={}", valueType.type.getTypeName(), field);
        }

        if (allowedValueClass.apply(valueType.rawClass)) return;

        if (valueType.rawClass.getPackage().getName().startsWith("java")) {
            throw Exceptions.error("field type is not supported, please contract arch team, type={}, field={}", valueType.type.getTypeName(), field);
        }

        if (field != null && !allowChildObject) {
            throw Exceptions.error("child object is not allowed, type={}, field={}", valueType.type, field);
        }

        visitObjectType(valueType, false);
    }

    private void visitList(TypeInspector listType, Field field) {
        if (!listType.isGenericList()) {
            throw Exceptions.error("list must be as List<T> and T must be class type, type={}", listType.type.getTypeName());
        }
        Class<?> valueClass = listType.listValueClass();
        visitValue(new TypeInspector(valueClass), field);
    }
}
