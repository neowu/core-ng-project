package core.framework.impl.web.controller;

import core.framework.util.Lists;
import core.framework.web.Interceptor;

import java.util.List;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public final class Interceptors {
    final List<Interceptor> interceptors = Lists.newArrayList();

    public void add(Interceptor interceptor) {
        if (interceptor.getClass().isSynthetic())
            throw new Error(format("interceptor class must not be anonymous class or lambda, please use static class, interceptorClass={}", interceptor.getClass().getCanonicalName()));

        interceptors.add(interceptor);
    }
}
