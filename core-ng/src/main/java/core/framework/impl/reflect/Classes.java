package core.framework.impl.reflect;


import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author neo
 */
public final class Classes {
    public static List<Field> instanceFields(Class<?> instanceClass) {
        // even though according to JDK doc, the fields returned is not in particular order, but common JDK impl follows field declaration order of source code
        return Arrays.stream(instanceClass.getDeclaredFields())
                     .filter(field -> !Modifier.isStatic(field.getModifiers()))
                     .collect(Collectors.toList());
    }

    public static List<Field> enumConstantFields(Class<?> enumClass) {
        return Arrays.stream(enumClass.getDeclaredFields())
                     .filter(Field::isEnumConstant)
                     .collect(Collectors.toList());
    }

    // return class name without package, e.g. for nested class will be EnclosingClass$NestedClass
    public static String className(Class<?> valueClass) {
        String name = valueClass.getName();
        int index = name.lastIndexOf('.');
        if (index < 0) return name;
        return name.substring(index + 1);
    }
}
