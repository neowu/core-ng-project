package core.framework.internal.bean;

import core.framework.internal.reflect.Classes;
import core.framework.util.Maps;

import java.util.Map;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public class BeanClassNameValidator {
    final Map<String, Class<?>> beanClasses = Maps.newHashMap();

    public void validate(Class<?> beanClass) {
        beanClasses.compute(Classes.className(beanClass), (key, previous) -> {
            if (previous != null && !previous.equals(beanClass))
                throw new Error(format("found bean class with duplicate name which can be confusing, please use different class name, previousClass={}, class={}",
                    previous.getCanonicalName(), beanClass.getCanonicalName()));
            return beanClass;
        });
    }
}
