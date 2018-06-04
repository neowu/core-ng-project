package core.framework.impl.web.bean;

import core.framework.impl.reflect.Classes;
import core.framework.util.Exceptions;
import core.framework.util.Maps;

import java.util.Map;

/**
 * @author neo
 */
public class BeanClassNameValidator {
    final Map<String, Class<?>> registeredClasses = Maps.newConcurrentHashMap();

    void validateBeanClass(Class<?> beanClass) {
        registeredClasses.compute(Classes.className(beanClass), (key, previous) -> {
            if (previous != null && !previous.equals(beanClass))
                throw Exceptions.error("found bean class with duplicate name which can be confusing, please use different class name, previousClass={}, class={}", previous.getCanonicalName(), beanClass.getCanonicalName());
            return beanClass;
        });
    }
}
