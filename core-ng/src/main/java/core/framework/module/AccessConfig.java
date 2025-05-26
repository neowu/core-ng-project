package core.framework.module;

import core.framework.internal.module.ModuleContext;
import core.framework.internal.web.http.IPAccessControl;
import core.framework.internal.web.http.IPv4Ranges;
import core.framework.internal.web.http.IPv6Ranges;
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
        IPAccessControl accessControl = accessControl();
        if (accessControl.allow != null) throw new Error("allowed cidrs is already configured");

        logger.info("allow http access, cidrs={}", cidrsLogParam(cidrs, 5));
        accessControl.allow = new IPv4Ranges(cidrs);
    }

    public void allowIPv6(List<String> cidrs) {
        IPAccessControl accessControl = accessControl();
        if (accessControl.allowIPv6 != null) throw new Error("allowed ipv6 cidrs is already configured");

        logger.info("allow ipv6 http access, cidrs={}", cidrsLogParam(cidrs, 5));
        accessControl.allowIPv6 = new IPv6Ranges(cidrs);
    }

    /**
     * Set denied cidr blocks, e.g. 192.168.0.1/24 or 192.168.1.1/32 for single ip
     *
     * @param cidrs cidr blocks
     */
    public void deny(List<String> cidrs) {
        IPAccessControl accessControl = accessControl();
        if (accessControl.deny != null) throw new Error("denied cidrs is already configured");

        logger.info("deny http access, cidrs={}", cidrsLogParam(cidrs, 5));
        accessControl.deny = new IPv4Ranges(cidrs);
    }

    public void denyIPv6(List<String> cidrs) {
        IPAccessControl accessControl = accessControl();
        if (accessControl.denyIPv6 != null) throw new Error("denied ipv6 cidrs is already configured");

        logger.info("deny ipv6 http access, cidrs={}", cidrsLogParam(cidrs, 5));
        accessControl.denyIPv6 = new IPv6Ranges(cidrs);
    }

    /**
     * Set denied ip range list, one cidr per line, comment starts with '#'
     *
     * @param classpath cidr file
     */
    public void denyFromFile(String classpath) {
        deny(new IPRangeFileParser(classpath).parse());
    }

    public void denyIPv6FromFile(String classpath) {
        denyIPv6(new IPRangeFileParser(classpath).parse());
    }

    private IPAccessControl accessControl() {
        if (context.httpServer.handlerContext.accessControl == null) {
            context.httpServer.handlerContext.accessControl = new IPAccessControl();
        }
        return context.httpServer.handlerContext.accessControl;
    }

    String cidrsLogParam(List<String> cidrs, int maxSize) {
        if (cidrs.size() <= maxSize) return String.valueOf(cidrs);
        return "[" + String.join(", ", cidrs.subList(0, maxSize)) + ", ...]";
    }
}
