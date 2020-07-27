package core.framework.internal.web.bean;

import core.framework.internal.json.JSONMapper;
import core.framework.internal.validate.Validator;

import java.io.IOException;

/**
 * @author neo
 */
public class BeanMapper<T> {
    final JSONMapper<T> mapper;
    final Validator validator;

    BeanMapper(Class<T> beanClass) {
        mapper = new JSONMapper<>(beanClass);
        validator = Validator.of(beanClass);
    }

    public byte[] toJSON(T bean) {
        validator.validate(bean, false);
        return mapper.toJSON(bean);
    }

    public T fromJSON(byte[] body) throws IOException {
        T bean = mapper.fromJSON(body);
        validator.validate(bean, false);
        return bean;
    }
}
