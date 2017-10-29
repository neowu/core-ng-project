package core.framework.impl.web;

import core.framework.util.Exceptions;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author neo
 */
public class ControllerClassValidator {
    private final Class<?> controllerClass;
    private final Method controllerMethod;

    public ControllerClassValidator(Class<?> controllerClass, Method controllerMethod) {
        this.controllerClass = controllerClass;
        this.controllerMethod = controllerMethod;
    }

    public void validate() {
        if (!Object.class.equals(controllerClass.getSuperclass())) {
            throw Exceptions.error("controller class must not have super class, class={}", controllerClass.getCanonicalName());
        }
        if (!Modifier.isPublic(controllerMethod.getModifiers())) {
            throw Exceptions.error("controller method must be public, method={}", controllerMethod.getDeclaringClass().getCanonicalName() + "." + controllerMethod.getName());
        }
    }
}
