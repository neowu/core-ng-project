package core.framework.internal.web.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static core.framework.log.Markers.errorCode;

/**
 * @author neo
 */
public final class WebServiceController {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebServiceController.class);

    // used by generated class, must be public
    public static void logDeprecation(String method) {
        LOGGER.warn(errorCode("DEPRECATION"), "web service has been deprecated, please notify consumer to update, method={}", method);
    }
}
