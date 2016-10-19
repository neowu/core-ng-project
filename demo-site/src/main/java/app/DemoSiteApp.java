package app;

import core.framework.api.App;
import core.framework.api.module.SystemModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author neo
 */
public class DemoSiteApp extends App {
    private final Logger logger = LoggerFactory.getLogger(DemoSiteApp.class);
    @Override
    protected void initialize() {
        load(new SystemModule("sys.properties"));

        loadProperties("app.properties");

        logger.warn("!!!!!!!! {} !!!!!!!!", requiredProperty("a.1"));


        load(new WebModule());

    }
}
