package core.framework.internal.inject;

import core.framework.inject.Inject;
import core.framework.internal.reflect.Fields;
import core.framework.util.Strings;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

/**
 * @author neo
 */
public class InjectValidator {
    // validate all cascaded fields with @Inject are properly assigned,
    // to prevent cases like someone added new @Inject field to one class but didn't realize the instance is not created thru DI

    private final Class<?> rootClass;
    private final Set<Object> visitedObjects = new HashSet<>();     // to prevent circular references
    private final Queue<Object> queue = new ArrayDeque<>();

    public InjectValidator(Object instance) {
        rootClass = instance.getClass();
        queue.add(instance);
    }

    public void validate() {
        while (true) {
            var instance = queue.poll();
            if (instance == null) return;

            visitedObjects.add(instance);

            Field[] fields = instance.getClass().getDeclaredFields();
            for (Field field : fields) {
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers)) continue;  // skip final field as final field enforces initialization

                Object value = fieldValue(field, instance);

                // 1. @Inject presents, not null -> stop as most likely injected by framework
                // 2. @Inject presents, null -> error, means parent bean is not autowired
                // 3. @Inject not present, not null -> add to queue to check object tree
                // 4. @Inject not present, null -> stop as it could be app managed field
                if (field.isAnnotationPresent(Inject.class)) {
                    if (value == null) throw new Error(Strings.format("field with @Inject is not bound to any value, rootClass={}, field={}", rootClass.getCanonicalName(), Fields.path(field)));
                } else if (value != null && shouldInspect(value)) {
                    queue.add(value);
                }
            }
        }
    }

    // only try to inspect relevant / application level classes
    private boolean shouldInspect(Object value) {
        Class<?> valueClass = value.getClass();
        if (valueClass.getPackageName().startsWith("java") || valueClass.isEnum()) return false;
        return !visitedObjects.contains(value);
    }

    private Object fieldValue(Field field, Object instance) {
        if (field.trySetAccessible()) {
            try {
                return field.get(instance);
            } catch (IllegalAccessException e) {
                throw new Error(e);
            }
        }
        return null;    // ignore non accessible fields
    }
}
