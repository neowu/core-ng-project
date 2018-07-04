package core.framework.impl.web.bean;

import core.framework.impl.json.JSONReader;
import core.framework.impl.json.JSONWriter;
import core.framework.impl.validate.Validator;

/**
 * @author neo
 */
class BeanMapper<T> {
    final JSONReader<T> reader;
    final JSONWriter<T> writer;
    final Validator validator;

    BeanMapper(Class<T> beanClass, Validator validator) {
        reader = JSONReader.of(beanClass);
        writer = JSONWriter.of(beanClass);
        this.validator = validator;
    }
}
