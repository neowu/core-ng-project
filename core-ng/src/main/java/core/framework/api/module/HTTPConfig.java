package core.framework.api.module;

import core.framework.api.web.ErrorHandler;
import core.framework.api.web.Interceptor;

/**
 * @author neo
 */
public interface HTTPConfig {
    void port(int port);

    void intercept(Interceptor interceptor);

    void errorHandler(ErrorHandler handler);
}
