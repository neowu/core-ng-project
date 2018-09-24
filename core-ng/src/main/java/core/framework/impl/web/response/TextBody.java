package core.framework.impl.web.response;

import core.framework.util.Strings;
import io.undertow.io.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * @author neo
 */
public final class TextBody implements Body {
    private final Logger logger = LoggerFactory.getLogger(TextBody.class);

    private final String text;

    public TextBody(String text) {
        this.text = text;
    }

    @Override
    public void send(Sender sender, ResponseHandlerContext context) {
        byte[] bytes = Strings.bytes(text);
        logger.debug("[response] body={}", text);
        sender.send(ByteBuffer.wrap(bytes));
    }
}
