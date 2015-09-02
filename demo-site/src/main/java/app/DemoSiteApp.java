package app;

import core.framework.api.AbstractApplication;
import core.framework.api.module.SystemModule;

/**
 * @author neo
 */
public class DemoSiteApp extends AbstractApplication {
    @Override
    protected void initialize() {
        load(new SystemModule("sys.properties"));

        load(new WebModule());
    }
}
