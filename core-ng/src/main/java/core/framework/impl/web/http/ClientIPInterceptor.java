package core.framework.impl.web.http;

import core.framework.web.Interceptor;
import core.framework.web.Invocation;
import core.framework.web.Response;

/**
 * @author neo
 */
public class ClientIPInterceptor implements Interceptor {
    private final IPAccessControl accessControl;

    public ClientIPInterceptor(String... cidrs) {
        accessControl = new IPAccessControl(cidrs);
    }

    @Override
    public Response intercept(Invocation invocation) throws Exception {
        String clientIP = invocation.context().request().clientIP();
        accessControl.validate(clientIP);
        return invocation.proceed();
    }
}
