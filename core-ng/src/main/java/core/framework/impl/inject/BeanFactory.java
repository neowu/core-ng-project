package core.framework.impl.inject;

import core.framework.api.util.Exceptions;
import core.framework.api.util.Maps;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @author neo
 */
public class BeanFactory {
    final Map<Key, Object> beans = Maps.newHashMap();

    public <T> T bindSupplier(Type type, String name, Supplier<T> supplier) {
        T instance = supplier.get();
        bind(type, name, instance);
        return instance;
    }

    public void bind(Type type, String name, Object instance) {
        if (instance == null) throw new Error("instance is null");

        if (!isTypeOf(instance, type))
            throw Exceptions.error("instance type doesn't match, type={}, instanceType={}", type, instance.getClass());

        Object previous = beans.put(new Key(type, name), instance);
        if (previous != null)
            throw Exceptions.error("duplicated bean found, type={}, name={}, previous={}", type.getTypeName(), name, previous);
    }

    public Set<Key> keys() {
        return beans.keySet();
    }

    public boolean registered(Type type, String name) {
        return beans.containsKey(new Key(type, name));
    }

    public <T> T bean(Type type, String name) {
        Key key = new Key(type, name);
        @SuppressWarnings("unchecked")
        T bean = (T) beans.get(key);
        if (bean == null) throw new Error("can not find bean, type=" + type + ", name=" + name);
        return bean;
    }

    public <T> T create(Class<T> instanceType) {
        if (instanceType.isInterface() || Modifier.isAbstract(instanceType.getModifiers()))
            throw new Error("instance type must be concrete class, instanceType=" + instanceType);

        try {
            T instance = construct(instanceType);
            inject(instance);
            return instance;
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException | RuntimeException e) {
            throw new Error("failed to build bean, instanceType=" + instanceType + ", error=" + e.getMessage(), e);
        }
    }

    private <T> T construct(Class<T> instanceType) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<?> targetConstructor = null;

        for (Constructor<?> constructor : instanceType.getDeclaredConstructors()) {
            if (constructor.isAnnotationPresent(Inject.class)) {
                if (targetConstructor != null)
                    throw new Error("@Inject can only be declared in one method, previous=" + targetConstructor + ", current=" + constructor);
                targetConstructor = constructor;
            }
        }
        try {
            if (targetConstructor == null) targetConstructor = instanceType.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new Error("require default constructor, instanceType=" + instanceType, e);
        }

        Object[] params = lookupParams(targetConstructor);

        return instanceType.cast(targetConstructor.newInstance(params));
    }

    private <T> void inject(T instance) throws IllegalAccessException, InvocationTargetException {
        Class visitorType = instance.getClass();
        while (!visitorType.equals(Object.class)) {
            for (Field field : visitorType.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    makeAccessible(field);

                    field.set(instance, lookupValue(field));
                }
            }

            for (Method method : visitorType.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Inject.class)) {
                    makeAccessible(method);

                    Object[] params = lookupParams(method);
                    method.invoke(instance, params);
                }
            }
            visitorType = visitorType.getSuperclass();
        }
    }

    private Object lookupValue(Field field) {
        Named name = field.getDeclaredAnnotation(Named.class);
        return bean(field.getGenericType(), name == null ? null : name.value());
    }

    private Object[] lookupParams(Executable method) {
        Class<?>[] paramTypes = method.getParameterTypes();
        Annotation[][] paramAnnotations = method.getParameterAnnotations();
        Object[] params = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            Type paramType = paramTypes[i];
            String name = name(paramAnnotations[i]);
            params[i] = bean(paramType, name);
        }
        return params;
    }

    private void makeAccessible(Field field) {
        AccessController.doPrivileged((PrivilegedAction<Field>) () -> {
            field.setAccessible(true);
            return field;
        });
    }

    private void makeAccessible(Method method) {
        AccessController.doPrivileged((PrivilegedAction<Method>) () -> {
            method.setAccessible(true);
            return method;
        });
    }

    private String name(Annotation[] paramAnnotation) {
        for (Annotation annotation : paramAnnotation) {
            if (annotation.annotationType().equals(Named.class)) {
                return ((Named) annotation).value();
            }
        }
        return null;
    }

    boolean isTypeOf(Object instance, Type type) {
        if (type instanceof Class) return ((Class) type).isInstance(instance);
        if (type instanceof ParameterizedType) return isTypeOf(instance, ((ParameterizedType) type).getRawType());
        throw Exceptions.error("not supported type, type={}", type);
    }
}
