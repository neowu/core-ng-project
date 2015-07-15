package app;

import app.message.CreateProductRequest;
import app.message.CreateProductRequestHandler;
import core.framework.api.Module;

/**
 * @author neo
 */
public class QueueModule extends Module {
    @Override
    protected void initialize() {
        queue().subscribe("rabbitmq://queue/test")
            .handle(CreateProductRequest.class, bind(CreateProductRequestHandler.class))
            .maxConcurrentHandlers(100);

        queue().publish("rabbitmq://queue/test", CreateProductRequest.class);
    }
}
