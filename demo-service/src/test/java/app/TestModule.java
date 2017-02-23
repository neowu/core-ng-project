package app;

import core.framework.api.AbstractTestModule;
import core.framework.api.http.HTTPClient;
import core.framework.api.http.HTTPClientBuilder;

/**
 * @author neo
 */
public class TestModule extends AbstractTestModule {
    @Override
    protected void initialize() {
        overrideBinding(HTTPClient.class, new HTTPClientBuilder().build());

        load(new DemoServiceApp());
    }
}
