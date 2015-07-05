package app.message;

import core.framework.api.queue.MessageHandler;
import core.framework.api.util.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author neo
 */
public class CreateProductRequestHandler implements MessageHandler<CreateProductRequest> {
    private final Logger logger = LoggerFactory.getLogger(CreateProductRequestHandler.class);

    @Override
    public void handle(CreateProductRequest message) throws Exception {
        logger.info("consumed message, message={}", JSON.toJSON(message));
    }
}
