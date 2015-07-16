package core.framework.impl.web.response;

import core.framework.api.http.ContentTypes;
import core.framework.api.util.Exceptions;
import core.framework.api.util.JSON;
import core.framework.api.util.Types;
import core.framework.api.web.ResponseImpl;
import core.framework.impl.web.BeanValidator;
import io.undertow.io.Sender;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
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
    public void handle(ResponseImpl response, HttpServerExchange exchange) {
        Object bean = ((BeanBody) response.body).bean;

        validateBeanClass(bean);

        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, ContentTypes.APPLICATION_JSON);

        Sender sender = exchange.getResponseSender();
        String responseText = JSON.toJSON(bean);
        logger.debug("responseText={}", responseText);
        sender.send(responseText);
    }

    // to validate response bean, since it can not get declaration type from instance, try to construct original type as much as it can.
    void validateBeanClass(Object bean) {
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
