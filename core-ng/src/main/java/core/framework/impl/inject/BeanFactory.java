package core.framework.impl.inject;

import core.framework.util.Exceptions;
import core.framework.util.Maps;
import core.framework.util.Types;

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

/**
 * @author neo
 */
public class BeanFactory {
    final Map<Key, Object> beans = Maps.newHashMap();

    public void bind(Type type, String name, Object instance) {
        if (instance == null) throw new Error("instance is null");

        if (!isTypeOf(instance, type))
            throw Exceptions.error("instance type doesn't match, type={}, instanceType={}", type, instance.getClass());

        Object previous = beans.put(new Key(type, name), instance);
        if (previous != null)
            throw Exceptions.error("found duplicate bean, type={}, name={}, previous={}", type.getTypeName(), name, previous);
    }

    public boolean registered(Type type, String name) {
        return beans.containsKey(new Key(type, name));
    }

    public <T> T bean(Type type, String name) {
        Key key = new Key(type, name);
        @SuppressWarnings("unchecked")
        T bean = (T) beans.get(key);
        if (bean == null) throw Exceptions.error("can not find bean, type={}, name={}", type, name);
        return bean;
    }

    public <T> T create(Class<T> instanceClass) {
        if (instanceClass.isInterface() || Modifier.isAbstract(instanceClass.getModifiers()))
            throw Exceptions.error("instance class must be concrete, class={}", instanceClass.getCanonicalName());

        try {
            T instance = construct(instanceClass);
            inject(instance);
            return instance;
        } catch (IllegalAccessException | InstantiationException | RuntimeException e) {
            throw Exceptions.error("failed to create bean, instanceClass={}, error={}", instanceClass, e.getMessage(), e);
        } catch (InvocationTargetException e) {
            throw Exceptions.error("failed to create bean, instanceClass={}, error={}", instanceClass, e.getTargetException().getMessage(), e);
        }
    }

    private <T> T construct(Class<T> instanceClass) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<?> targetConstructor = null;

        for (Constructor<?> constructor : instanceClass.getDeclaredConstructors()) {
            if (constructor.isAnnotationPresent(Inject.class)) {
                if (targetConstructor != null)
                    throw Exceptions.error("only one constructor can have @Inject, previous={}, current={}", targetConstructor, constructor);
                targetConstructor = constructor;
            }
        }
        try {
            if (targetConstructor == null) targetConstructor = instanceClass.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw Exceptions.error("default constructor is required, class={}", instanceClass, e);
        }

        Object[] params = lookupParams(targetConstructor);

        return instanceClass.cast(targetConstructor.newInstance(params));
    }

    private <T> void inject(T instance) throws IllegalAccessException, InvocationTargetException {
        Class<?> visitorType = instance.getClass();
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
        Type fieldType = stripOutOwnerType(field.getGenericType());
        return bean(fieldType, name == null ? null : name.value());
    }

    private Object[] lookupParams(Executable method) {
        Type[] paramTypes = method.getGenericParameterTypes();
        Annotation[][] paramAnnotations = method.getParameterAnnotations();
        Object[] params = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            Type paramType = stripOutOwnerType(paramTypes[i]);
            String name = name(paramAnnotations[i]);
            params[i] = bean(paramType, name);
        }
        return params;
    }

    private Type stripOutOwnerType(Type paramType) {    // type from field/method params could has owner type, which is not used in bind/key
        if (paramType instanceof ParameterizedType)
            return Types.generic((Class<?>) ((ParameterizedType) paramType).getRawType(), ((ParameterizedType) paramType).getActualTypeArguments());
        return paramType;
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

    private boolean isTypeOf(Object instance, Type type) {
        if (type instanceof Class) return ((Class) type).isInstance(instance);
        if (type instanceof ParameterizedType) return isTypeOf(instance, ((ParameterizedType) type).getRawType());
        throw Exceptions.error("not supported type, type={}", type);
    }
}
