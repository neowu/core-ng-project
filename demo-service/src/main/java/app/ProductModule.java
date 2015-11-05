package app;

import app.product.api.ProductView;
import app.product.api.ProductWebService;
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
    }
}
