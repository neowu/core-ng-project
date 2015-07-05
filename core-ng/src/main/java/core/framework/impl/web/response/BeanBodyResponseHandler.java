package core.framework.impl.web.response;

import core.framework.api.http.ContentTypes;
import core.framework.api.util.Exceptions;
import core.framework.api.util.JSON;
import core.framework.api.util.Maps;
import core.framework.api.web.ResponseImpl;
import core.framework.impl.web.BeanTypeValidator;
import io.undertow.io.Sender;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public class BeanBodyResponseHandler implements BodyHandler {
    private final Logger logger = LoggerFactory.getLogger(BeanBodyResponseHandler.class);

    private final Map<Class<?>, Boolean> validations = Maps.newConcurrentHashMap();

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

    void validateBeanClass(Object bean) {
        Class<?> beanClass = bean.getClass();
        if (bean instanceof List && !((List) bean).isEmpty()) {
            Object item = ((List) bean).get(0);
            if (item == null) throw Exceptions.error("response bean must not be list with null items, list={}", bean);
            beanClass = item.getClass();
        }

        if (!validations.containsKey(beanClass)) {
            new BeanTypeValidator(beanClass).validate();
            validations.putIfAbsent(beanClass, true);
        }
    }
}
