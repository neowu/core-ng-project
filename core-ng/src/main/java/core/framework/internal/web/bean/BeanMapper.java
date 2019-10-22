package core.framework.internal.web.bean;

import core.framework.internal.json.JSONMapper;
import core.framework.internal.validate.Validator;

/**
 * @author neo
 */
class BeanMapper<T> {
    final JSONMapper<T> mapper;
    final Validator validator;

    BeanMapper(Class<T> beanClass) {
        mapper = new JSONMapper<>(beanClass);
        validator = Validator.of(beanClass);
    }
}
