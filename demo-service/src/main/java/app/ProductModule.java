package app;

import app.product.api.ProductView;
import app.product.api.ProductWebService;
import app.product.queue.TestMessage;
import app.product.queue.TestMessageHandler;
import app.product.service.ProductService;
import app.product.web.ProductWebServiceImpl;
import core.framework.api.Module;

import java.time.Duration;

/**
 * @author neo
 */
public class ProductModule extends Module {
    @Override
    protected void initialize() {
        cache().add(ProductView.class, Duration.ofSeconds(60));

        bind(ProductService.class);

        api().service(ProductWebService.class, bind(ProductWebServiceImpl.class));

        queue().hosts("54.173.158.3");
        queue().subscribe("neo-test").handle(TestMessage.class, bind(TestMessageHandler.class));
    }
}
