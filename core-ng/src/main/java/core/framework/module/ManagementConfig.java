package core.framework.module;

import core.framework.impl.web.HTTPServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * @author neo
 */
public final class ManagementConfig {
    private final Logger logger = LoggerFactory.getLogger(ManagementConfig.class);
    private final HTTPServer httpServer;

    ManagementConfig(HTTPServer httpServer) {
        this.httpServer = httpServer;
    }

    public void allowCIDR(String... cidrs) {
        logger.info("limit remote access to management controllers, cidrs={}", Arrays.toString(cidrs));
        httpServer.managementAccessControl.allowCIDR(cidrs);
    }
}
