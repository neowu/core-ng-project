package core.framework.impl.web.bean;

import core.framework.impl.validate.Validator;
import core.framework.internal.json.JSONMapper;

/**
 * @author neo
 */
class BeanMapper<T> {
    final JSONMapper<T> mapper;
    final Validator validator;

    BeanMapper(Class<T> beanClass, Validator validator) {
        mapper = new JSONMapper<>(beanClass);
        this.validator = validator;
    }
}
