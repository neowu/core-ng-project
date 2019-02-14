package core.framework.impl.web.bean;

import core.framework.internal.json.JSONMapper;
import core.framework.internal.validate.Validator;

/**
 * @author neo
 */
public class BeanMapper<T> {
    final JSONMapper<T> mapper;
    final Validator validator;

    public BeanMapper(Class<T> beanClass) {
        mapper = new JSONMapper<>(beanClass);
        this.validator = new Validator(beanClass);
    }
}
