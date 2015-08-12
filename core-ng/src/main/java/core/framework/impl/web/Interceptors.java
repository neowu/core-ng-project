package core.framework.impl.web;

import core.framework.api.util.Exceptions;
import core.framework.api.util.Lists;
import core.framework.api.web.Interceptor;

import java.util.List;

/**
 * @author neo
 */
public class Interceptors {
    final List<Interceptor> interceptors = Lists.newArrayList();

    public void add(Interceptor interceptor) {
        if (interceptor.getClass().isSynthetic())
            throw Exceptions.error("interceptor class must not be anonymous class or lambda, please create static class, interceptorClass={}", interceptor.getClass().getCanonicalName());

        interceptors.add(interceptor);
    }
}
