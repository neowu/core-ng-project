package core.framework.internal.web.response;

import core.framework.util.Strings;
import io.undertow.io.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * @author neo
 */
public final class BeanBody implements Body {
    private static final Logger LOGGER = LoggerFactory.getLogger(BeanBody.class);
    public final Object bean;

    public BeanBody(Object bean) {
        this.bean = bean;
    }

    @Override
    public long send(Sender sender, ResponseHandlerContext context) {
        String body = context.writer.toJSON(bean);
        LOGGER.debug("[response] body={}", body);
        byte[] bytes = Strings.bytes(body);
        sender.send(ByteBuffer.wrap(bytes));
        return bytes.length;
    }
}
