package core.framework.module;

import core.framework.api.web.service.Path;
import core.framework.http.HTTPClient;
import core.framework.http.HTTPClientBuilder;
import core.framework.http.HTTPMethod;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.web.ControllerActionBuilder;
import core.framework.impl.web.ControllerHolder;
import core.framework.impl.web.api.OpenAPIManager;
import core.framework.impl.web.bean.RequestBeanMapper;
import core.framework.impl.web.management.OpenAPIController;
import core.framework.impl.web.service.HTTPMethods;
import core.framework.impl.web.service.WebServiceClient;
import core.framework.impl.web.service.WebServiceClientBuilder;
import core.framework.impl.web.service.WebServiceControllerBuilder;
import core.framework.impl.web.service.WebServiceImplValidator;
import core.framework.impl.web.service.WebServiceInterfaceValidator;
import core.framework.web.Controller;
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

    APIConfig(ModuleContext context) {
        this.context = context;
        state = context.config.api();
    }

    public <T> void service(Class<T> serviceInterface, T service) {
        logger.info("create api service, interface={}", serviceInterface.getCanonicalName());
        new WebServiceInterfaceValidator(serviceInterface,
                context.httpServer.handler.requestBeanMapper,
                context.httpServer.handler.responseBeanTypeValidator).validate();
        new WebServiceImplValidator<>(serviceInterface, service).validate();

        for (Method method : serviceInterface.getMethods()) {
            HTTPMethod httpMethod = HTTPMethods.httpMethod(method);
            String path = method.getDeclaredAnnotation(Path.class).value();
            Controller controller = new WebServiceControllerBuilder<>(serviceInterface, service, method).build();
            try {
                Class<?>[] parameterTypes = method.getParameterTypes();
                Class<?> serviceClass = service.getClass();
                Method targetMethod = serviceClass.getMethod(method.getName(), parameterTypes);
                String controllerInfo = serviceClass.getCanonicalName() + "." + targetMethod.getName();
                String action = new ControllerActionBuilder(httpMethod, path).build();
                context.httpServer.handler.route.add(httpMethod, path, new ControllerHolder(controller, targetMethod, controllerInfo, action, false));
            } catch (NoSuchMethodException e) {
                throw new Error("failed to find impl method", e);
            }
        }

        openAPIManager().serviceInterfaces.add(serviceInterface);
    }

    public <T> APIClientConfig client(Class<T> serviceInterface, String serviceURL) {
        logger.info("create api service client, interface={}, serviceURL={}", serviceInterface.getCanonicalName(), serviceURL);
        RequestBeanMapper requestBeanMapper = context.httpServer.handler.requestBeanMapper;
        new WebServiceInterfaceValidator(serviceInterface, requestBeanMapper, context.httpServer.handler.responseBeanTypeValidator).validate();

        HTTPClient httpClient = httpClient();
        WebServiceClient webServiceClient = new WebServiceClient(serviceURL, httpClient, requestBeanMapper, context.logManager);
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

    private OpenAPIManager openAPIManager() {
        if (state.openAPIManager == null) {
            state.openAPIManager = new OpenAPIManager(context.logManager.appName);
            if (!context.isTest()) {
                context.route(HTTPMethod.GET, "/_sys/api", new OpenAPIController(state.openAPIManager), true);
            }
        }
        return state.openAPIManager;
    }

    public static class State {
        HTTPClient httpClient;
        OpenAPIManager openAPIManager;
    }
}
