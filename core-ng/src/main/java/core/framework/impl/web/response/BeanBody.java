package core.framework.impl.web.response;

import core.framework.impl.log.filter.BytesParam;
import core.framework.impl.validate.ValidationException;
import core.framework.json.JSON;
import io.undertow.io.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

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
        try {
            byte[] body = context.responseBeanMapper.toJSON(bean);
            logger.debug("[response] body={}", new BytesParam(body));
            sender.send(ByteBuffer.wrap(body));
        } catch (ValidationException e) {
            logger.debug("failed to validate response bean, bean={}", JSON.toJSON(bean));  // log invalid bean for troubleshooting
            throw e;
        }
    }
}
