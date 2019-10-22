package core.framework.internal.web.controller;

import core.framework.internal.reflect.Methods;

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
            throw new Error("controller class must not have super class, class=" + controllerClass.getCanonicalName());
        }
        if (!Modifier.isPublic(controllerMethod.getModifiers())) {
            throw new Error("controller method must be public, method=" + Methods.path(controllerMethod));
        }
    }
}
