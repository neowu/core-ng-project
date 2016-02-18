package core.framework.impl.web.response;

import core.framework.api.util.Exceptions;
import core.framework.api.util.Types;
import core.framework.api.web.ResponseImpl;
import core.framework.impl.json.JSONMapper;
import core.framework.impl.log.LogParam;
import core.framework.impl.web.BeanValidator;
import core.framework.impl.web.request.RequestImpl;
import io.undertow.io.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * @author neo
 */
public class BeanBodyResponseHandler implements BodyHandler {
    private final Logger logger = LoggerFactory.getLogger(BeanBodyResponseHandler.class);

    private final BeanValidator validator;

    public BeanBodyResponseHandler(BeanValidator validator) {
        this.validator = validator;
    }

    @Override
    public void handle(ResponseImpl response, Sender sender, RequestImpl request) {
        Object bean = ((BeanBody) response.body).bean;
        validateBeanType(bean);
        byte[] body = JSONMapper.toJSON(bean);
        logger.debug("[response] body={}", LogParam.of(body));
        sender.send(ByteBuffer.wrap(body));
    }

    // to validate response bean, since it can not get declaration type from instance, try to construct original type as much as it can.
    void validateBeanType(Object bean) {
        Type instanceType;

        if (bean instanceof List) {
            List<?> list = (List<?>) bean;
            if (list.isEmpty()) return; // no type info can be used
            Object item = ((List) bean).get(0);
            if (item == null) throw Exceptions.error("response bean must not be list with null item, list={}", bean);
            instanceType = Types.list(item.getClass());
        } else {
            instanceType = bean.getClass();
        }

        validator.register(instanceType);
    }
}
