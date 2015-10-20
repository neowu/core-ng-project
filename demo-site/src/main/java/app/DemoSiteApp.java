package app;

import core.framework.api.App;
import core.framework.api.module.SystemModule;

/**
 * @author neo
 */
public class DemoSiteApp extends App {
    @Override
    protected void initialize() {
        load(new SystemModule("sys.properties"));

        redis().host("localhost");

        load(new WebModule());
    }
}
