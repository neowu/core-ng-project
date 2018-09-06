package core.framework.impl.web.response;

import core.framework.impl.log.filter.JSONParam;
import io.undertow.io.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

import static java.nio.charset.StandardCharsets.UTF_8;

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
        byte[] body = context.responseBeanMapper.toJSON(bean);
        logger.debug("[response] body={}", new JSONParam(body, UTF_8));
        sender.send(ByteBuffer.wrap(body));
    }
}
