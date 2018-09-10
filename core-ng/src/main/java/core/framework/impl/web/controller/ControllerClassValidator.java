package core.framework.impl.web.controller;

import core.framework.impl.reflect.Methods;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static core.framework.util.Strings.format;

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
            throw new Error(format("controller class must not have super class, class={}", controllerClass.getCanonicalName()));
        }
        if (!Modifier.isPublic(controllerMethod.getModifiers())) {
            throw new Error(format("controller method must be public, method={}", Methods.path(controllerMethod)));
        }
    }
}
