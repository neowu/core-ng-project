package app;

import core.framework.http.HTTPClient;
import core.framework.test.module.AbstractTestModule;

import static org.mockito.Mockito.mock;

public class TestModule extends AbstractTestModule {
    @Override
    protected void initialize() {
        overrideBinding(HTTPClient.class, mock(HTTPClient.class));

        load(new MonitorApp());
    }
}
