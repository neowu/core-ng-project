package core.framework.module;

import core.framework.impl.web.site.WebSecurityInterceptor;

/**
 * @author neo
 */
public class WebSecurityConfig {
    final WebSecurityInterceptor interceptor;

    WebSecurityConfig(WebSecurityInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    public void contentSecurityPolicy(String policy) {
        interceptor.contentSecurityPolicy = policy;
    }
}
