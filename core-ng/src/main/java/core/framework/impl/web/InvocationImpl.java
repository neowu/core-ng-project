package core.framework.impl.web;

import core.framework.web.Interceptor;
import core.framework.web.Invocation;
import core.framework.web.Request;
import core.framework.web.Response;
import core.framework.web.WebContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author neo
 */
final class InvocationImpl implements Invocation {
    private final Logger logger = LoggerFactory.getLogger(InvocationImpl.class);

    private final ControllerHolder controller;
    private final Interceptors interceptors;
    private final Request request;
    private final WebContextImpl context;
    private int currentStack;

    InvocationImpl(ControllerHolder controller, Interceptors interceptors, Request request, WebContextImpl context) {
        this.controller = controller;
        this.interceptors = interceptors;
        this.request = request;
        this.context = context;
    }

    @Override
    public <T extends Annotation> T annotation(Class<T> annotationClass) {
        Method controllerMethod = controller.targetMethod;
        T annotation = controllerMethod.getDeclaredAnnotation(annotationClass);
        if (annotation == null)
            annotation = controllerMethod.getDeclaringClass().getDeclaredAnnotation(annotationClass);
        return annotation;
    }

    @Override
    public WebContext context() {
        return context;
    }

    @Override
    public Response proceed() throws Exception {
        if (controller.skipInterceptor || currentStack >= interceptors.interceptors.size()) {
            logger.debug("execute controller, controller={}", controller.controllerInfo);
            return controller.controller.execute(request);
        } else {
            Interceptor interceptor = interceptors.interceptors.get(currentStack);
            currentStack++;
            logger.debug("intercept, interceptorClass={}", interceptor.getClass().getCanonicalName());
            return interceptor.intercept(this);
        }
    }
}
