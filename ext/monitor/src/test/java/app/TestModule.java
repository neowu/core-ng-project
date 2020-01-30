package app;

import core.framework.http.HTTPClient;
import core.framework.test.module.AbstractTestModule;
import core.framework.util.ClasspathResources;

import static org.mockito.Mockito.mock;

public class TestModule extends AbstractTestModule {
    @Override
    protected void initialize() {
        overrideBinding(HTTPClient.class, mock(HTTPClient.class));
        System.setProperty("app.monitor.config", ClasspathResources.text("monitor.json"));

        load(new MonitorApp());
    }
}
