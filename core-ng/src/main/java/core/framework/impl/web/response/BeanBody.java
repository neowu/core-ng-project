package core.framework.impl.web.response;

import core.framework.impl.json.JSONMapper;
import core.framework.impl.log.LogParam;
import core.framework.impl.web.bean.ResponseBeanTypeValidator;
import core.framework.util.Exceptions;
import core.framework.util.Types;
import io.undertow.io.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;

/**
 * @author neo
 */
public final class BeanBody implements Body {
    public final Object bean;
    private final Logger logger = LoggerFactory.getLogger(BeanBody.class);

    public BeanBody(Object bean) {
        this.bean = bean;
    }

    @Override
    public void send(Sender sender, ResponseHandlerContext context) {
        validateBeanType(context.validator);
        byte[] body = JSONMapper.toJSON(bean);
        logger.debug("[response] body={}", LogParam.of(body));
        sender.send(ByteBuffer.wrap(body));
    }

    // to validate response bean, since it can not get declaration type from instance, try to construct original type as much as it can.
    void validateBeanType(ResponseBeanTypeValidator validator) {
        Type beanType;

        if (bean == null) throw new Error("bean must not be null");

        if (bean instanceof List) {
            List<?> list = (List<?>) bean;
            if (list.isEmpty()) return; // no type info can be used
            Object item = ((List) bean).get(0);
            if (item == null) throw Exceptions.error("response bean must not be list with null item, list={}", bean);
            beanType = Types.list(item.getClass());
        } else if (bean instanceof Optional) {
            Optional<?> optional = (Optional) bean;
            if (!optional.isPresent()) return;
            beanType = Types.generic(Optional.class, optional.get().getClass());
        } else {
            beanType = bean.getClass();
        }

        validator.validate(beanType);
    }
}
