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
import core.framework.impl.web.service.WebServiceClient;
import core.framework.impl.web.service.WebServiceClientBuilder;
import core.framework.impl.web.service.WebServiceControllerBuilder;
import core.framework.impl.web.service.WebServiceImplValidator;
import core.framework.impl.web.service.WebServiceInterfaceValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * @author neo
 */
public final class APIConfig {
    private final Logger logger = LoggerFactory.getLogger(APIConfig.class);
    private final ModuleContext context;
    private final State state;

    public APIConfig(ModuleContext context) {
        this.context = context;
        state = context.config.api();
    }

    public <T> void service(Class<T> serviceInterface, T service) {
        logger.info("create api service, interface={}", serviceInterface.getCanonicalName());
        BeanValidator validator = context.httpServer.handler.validator;
        new WebServiceInterfaceValidator(serviceInterface, validator).validate();
        new WebServiceImplValidator<>(serviceInterface, service).validate();

        Method[] methods = serviceInterface.getDeclaredMethods();
        for (Method method : methods) {
            HTTPMethod httpMethod = HTTPMethodHelper.httpMethod(method);
            String path = method.getDeclaredAnnotation(Path.class).value();
            Controller controller = new WebServiceControllerBuilder<>(serviceInterface, service, method).build();
            try {
                Class<?>[] parameterTypes = method.getParameterTypes();
                Method targetMethod = service.getClass().getMethod(method.getName(), parameterTypes);
                context.httpServer.handler.route.add(httpMethod, path, new ControllerHolder(controller, targetMethod));
            } catch (NoSuchMethodException e) {
                throw new Error("failed to find impl method", e);
            }
        }
    }

    public <T> APIClientConfig client(Class<T> serviceInterface, String serviceURL) {
        logger.info("create api service client, interface={}, serviceURL={}", serviceInterface.getCanonicalName(), serviceURL);
        BeanValidator validator = context.httpServer.handler.validator;
        new WebServiceInterfaceValidator(serviceInterface, validator).validate();

        HTTPClient httpClient = httpClient();
        WebServiceClient webServiceClient = new WebServiceClient(serviceURL, httpClient, validator, context.logManager);
        T client = createWebServiceClient(serviceInterface, webServiceClient);
        context.beanFactory.bind(serviceInterface, null, client);
        return new APIClientConfig(webServiceClient);
    }

    private <T> T createWebServiceClient(Class<T> serviceInterface, WebServiceClient webServiceClient) {
        if (context.isTest()) {
            return context.mockFactory.create(serviceInterface);
        } else {
            return new WebServiceClientBuilder<>(serviceInterface, webServiceClient).build();
        }
    }

    private HTTPClient httpClient() {
        if (state.httpClient == null) {
            HTTPClient httpClient = new HTTPClientBuilder().userAgent("APIClient").build();
            context.shutdownHook.add(httpClient::close);
            state.httpClient = httpClient;
        }
        return state.httpClient;
    }

    public static class State {
        HTTPClient httpClient;
    }
}
