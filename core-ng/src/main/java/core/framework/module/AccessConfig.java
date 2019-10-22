package core.framework.module;

import core.framework.internal.module.ModuleContext;
import core.framework.internal.web.http.IPv4AccessControl;
import core.framework.internal.web.http.IPv4Ranges;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author neo
 */
public final class AccessConfig {
    private final Logger logger = LoggerFactory.getLogger(AccessConfig.class);
    private final ModuleContext context;

    AccessConfig(ModuleContext context) {
        this.context = context;
    }

    /**
     * Set allowed cidr blocks, e.g. 192.168.0.1/24 or 192.168.1.1/32 for single ip
     *
     * @param cidrs cidr blocks
     */
    public void allow(List<String> cidrs) {
        IPv4AccessControl accessControl = accessControl();
        if (accessControl.allow != null) throw new Error("allowed cidrs is already configured");

        logger.info("allow http access, cidrs={}", cidrsLogParam(cidrs, 5));
        accessControl.allow = new IPv4Ranges(cidrs);
    }

    /**
     * Set denied cidr blocks, e.g. 192.168.0.1/24 or 192.168.1.1/32 for single ip
     *
     * @param cidrs cidr blocks
     */
    public void deny(List<String> cidrs) {
        IPv4AccessControl accessControl = accessControl();
        if (accessControl.deny != null) throw new Error("denied cidrs is already configured");

        logger.info("deny http access, cidrs={}", cidrsLogParam(cidrs, 5));
        accessControl.deny = new IPv4Ranges(cidrs);
    }

    /**
     * Set denied ip range list, one cidr per line, comment starts with '#'
     *
     * @param classpath cidr file
     */
    public void denyFromFile(String classpath) {
        deny(new IPv4RangeFileParser(classpath).parse());
    }

    private IPv4AccessControl accessControl() {
        if (context.httpServer.handler.accessControl == null) {
            context.httpServer.handler.accessControl = new IPv4AccessControl();
        }
        return context.httpServer.handler.accessControl;
    }

    String cidrsLogParam(List<String> cidrs, int maxSize) {
        if (cidrs.size() <= maxSize) return String.valueOf(cidrs);
        return "[" + String.join(", ", cidrs.subList(0, maxSize)) + ", ...]";
    }
}
