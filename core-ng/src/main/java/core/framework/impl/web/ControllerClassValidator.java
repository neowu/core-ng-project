package core.framework.impl.web;

import core.framework.api.util.Exceptions;

/**
 * @author neo
 */
public class ControllerClassValidator {
    private final Class<?> controllerClass;

    public ControllerClassValidator(Class<?> controllerClass) {
        this.controllerClass = controllerClass;
    }

    public void validate() {
        if (!Object.class.equals(controllerClass.getSuperclass())) {
            throw Exceptions.error("controller class must not have super class, class={}", controllerClass.getCanonicalName());
        }
    }
}
