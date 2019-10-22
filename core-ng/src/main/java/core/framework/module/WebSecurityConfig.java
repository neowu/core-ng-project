package core.framework.module;

import core.framework.internal.web.site.WebSecurityInterceptor;

/**
 * @author neo
 */
public class WebSecurityConfig {
    final WebSecurityInterceptor interceptor;

    WebSecurityConfig(WebSecurityInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    public void contentSecurityPolicy(String policy) {
        if (policy == null) throw new Error("policy must not be null");
        interceptor.contentSecurityPolicy = policy;
    }
}
