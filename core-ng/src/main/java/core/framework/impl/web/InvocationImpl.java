package core.framework.impl.web;

import core.framework.api.web.Interceptor;
import core.framework.api.web.Invocation;
import core.framework.api.web.Request;
import core.framework.api.web.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public class InvocationImpl implements Invocation {
    private final Logger logger = LoggerFactory.getLogger(InvocationImpl.class);

    private final ControllerProxy controller;
    private final List<Interceptor> interceptors;
    private final Request request;
    private final WebContextImpl webContext;
    private int currentStack;

    public InvocationImpl(ControllerProxy controller, List<Interceptor> interceptors, Request request, WebContextImpl webContext) {
        this.controller = controller;
        this.interceptors = interceptors;
        this.request = request;
        this.webContext = webContext;
    }

    @Override
    public <T extends Annotation> T controllerAnnotation(Class<T> annotationClass) {
        Method controllerMethod = controller.targetMethod;
        T annotation = controllerMethod.getDeclaredAnnotation(annotationClass);
        if (annotation == null)
            annotation = controllerMethod.getDeclaringClass().getDeclaredAnnotation(annotationClass);
        return annotation;
    }

    @Override
    public Request request() {
        return request;
    }

    @Override
    public void putContext(String key, Object value) {
        Map<String, Object> context = webContext.context.get();
        context.put(key, value);
    }

    @Override
    public Response proceed() throws Exception {
        if (currentStack >= interceptors.size()) {
            logger.debug("execute controller, controllerClass={}, controllerMethod={}", controller.targetClassName, controller.targetMethodName);
            return controller.controller.execute(request);
        } else {
            Interceptor interceptor = interceptors.get(currentStack);
            currentStack++;
            logger.debug("intercept, interceptorClass={}", interceptor.getClass().getCanonicalName());
            return interceptor.intercept(this);
        }
    }
}
