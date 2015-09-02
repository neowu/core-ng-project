package app.web.interceptor;

import core.framework.api.web.Interceptor;
import core.framework.api.web.Invocation;
import core.framework.api.web.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author neo
 */
public class TestInterceptor implements Interceptor {
    private final Logger logger = LoggerFactory.getLogger(TestInterceptor.class);

    @Override
    public Response intercept(Invocation invocation) throws Exception {
        Protected annotation = invocation.annotation(Protected.class);
        if (annotation != null) {
            logger.debug("detected @Protected");
        }
        return invocation.proceed();
    }
}
