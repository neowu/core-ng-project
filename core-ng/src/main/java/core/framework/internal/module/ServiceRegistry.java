package core.framework.internal.module;

import core.framework.internal.bean.BeanClassNameValidator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author neo
 */
public class ServiceRegistry {
    public final Set<Class<?>> serviceInterfaces = new LinkedHashSet<>();
    public final Set<Class<?>> beanClasses = new LinkedHashSet<>();  // custom bean classes not referred by service interfaces, e.g. via controller, to publish via /_sys/api
    public BeanClassNameValidator beanClassNameValidator = new BeanClassNameValidator();
}
