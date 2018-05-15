package core.framework.impl.reflect;


import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author neo
 */
public final class Classes {
    public static List<Field> instanceFields(Class<?> instanceClass) {
        Field[] fields = instanceClass.getDeclaredFields();
        List<Field> instanceFields = new ArrayList<>(fields.length);
        for (Field field : fields) {
            if (!Modifier.isStatic(field.getModifiers())) instanceFields.add(field);
        }
        return instanceFields;      // even though according to JDK doc, the fields returned is not in particular order, but common JDK impl uses order as source code
    }

    public static <T> Optional<Constructor<T>> constructor(Class<?> instanceClass, Type... paramTypes) {
        @SuppressWarnings("unchecked")
        Constructor<T>[] constructors = (Constructor<T>[]) instanceClass.getDeclaredConstructors();
        for (Constructor<T> constructor : constructors) {
            if (Arrays.equals(constructor.getGenericParameterTypes(), paramTypes)) return Optional.of(constructor);
        }
        return Optional.empty();
    }
}
