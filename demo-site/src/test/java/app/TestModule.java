package app;

import core.framework.api.AbstractTestModule;

/**
 * @author neo
 */
public class TestModule extends AbstractTestModule {
    @Override
    protected void initialize() {
        load(new DemoSiteApp());
    }
}
