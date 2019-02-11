package core.framework.impl.web.bean;

import core.framework.internal.json.JSONMapper;
import core.framework.internal.validate.Validator;

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
