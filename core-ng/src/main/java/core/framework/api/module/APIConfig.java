package core.framework.api.module;

import core.framework.api.http.HTTPClient;
import core.framework.api.http.HTTPClientBuilder;
import core.framework.api.http.HTTPMethod;
import core.framework.api.web.Controller;
import core.framework.api.web.service.Path;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.web.BeanValidator;
import core.framework.impl.web.ControllerHolder;
import core.framework.impl.web.service.HTTPMethodHelper;
import core.framework.impl.web.service.ServiceControllerBuilder;
import core.framework.impl.web.service.ServiceInterfaceValidator;
import core.framework.impl.web.service.WebServiceClient;
import core.framework.impl.web.service.WebServiceClientBuilder;
import core.framework.impl.web.service.WebServiceClientImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * @author neo
 */
public final class APIConfig {
    private final Logger logger = LoggerFactory.getLogger(APIConfig.class);
    private final ModuleContext context;

    public APIConfig(ModuleContext context) {
        this.context = context;
    }

    public <T> void service(Class<T> serviceInterface, T service) {
        logger.info("create api service, interface={}", serviceInterface.getCanonicalName());
        BeanValidator validator = context.httpServer.handler.validator;
        new ServiceInterfaceValidator(serviceInterface, validator).validate();

        Method[] methods = serviceInterface.getDeclaredMethods();
        for (Method method : methods) {
            HTTPMethod httpMethod = HTTPMethodHelper.httpMethod(method);
            String path = method.getDeclaredAnnotation(Path.class).value();
            Controller controller = new ServiceControllerBuilder<>(serviceInterface, service, method).build();
            try {
                Method targetMethod = service.getClass().getMethod(method.getName(), method.getParameterTypes());
                context.httpServer.handler.route.add(httpMethod, path, new ControllerHolder(controller, targetMethod));
            } catch (NoSuchMethodException e) {
                throw new Error("failed to find impl method", e);
            }
        }
    }

    public <T> WebServiceClientConfig client(Class<T> serviceInterface, String serviceURL) {
        logger.info("create api service client, interface={}, serviceURL={}", serviceInterface.getCanonicalName(), serviceURL);
        BeanValidator validator = context.httpServer.handler.validator;
        new ServiceInterfaceValidator(serviceInterface, validator).validate();

        if (context.isTest()) {
            context.beanFactory.bind(serviceInterface, null, context.mockFactory.create(serviceInterface));
            return new WebServiceClientConfig(context, null);
        } else {
            HTTPClient httpClient = httpClient();
            WebServiceClient webServiceClient = new WebServiceClientImpl(serviceURL, httpClient, validator, context.logManager);
            T client = new WebServiceClientBuilder<>(serviceInterface, webServiceClient).build();
            context.beanFactory.bind(serviceInterface, null, client);
            return new WebServiceClientConfig(context, webServiceClient);
        }
    }

    private HTTPClient httpClient() {
        if (context.beanFactory.registered(HTTPClient.class, null))
            return context.beanFactory.bean(HTTPClient.class, null);

        HTTPClient httpClient = new HTTPClientBuilder().build();
        context.beanFactory.bind(HTTPClient.class, null, httpClient);
        context.shutdownHook.add(httpClient::close);
        return httpClient;
    }
}
