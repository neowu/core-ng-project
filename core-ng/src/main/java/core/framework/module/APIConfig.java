package core.framework.module;

import core.framework.api.web.service.Path;
import core.framework.http.HTTPClient;
import core.framework.http.HTTPClientBuilder;
import core.framework.http.HTTPMethod;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.web.ControllerHolder;
import core.framework.impl.web.bean.RequestBeanMapper;
import core.framework.impl.web.management.APIController;
import core.framework.impl.web.service.HTTPMethods;
import core.framework.impl.web.service.WebServiceClient;
import core.framework.impl.web.service.WebServiceClientBuilder;
import core.framework.impl.web.service.WebServiceControllerBuilder;
import core.framework.impl.web.service.WebServiceImplValidator;
import core.framework.impl.web.service.WebServiceInterfaceValidator;
import core.framework.util.ASCII;
import core.framework.web.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.time.Duration;

/**
 * @author neo
 */
public final class APIConfig {
    private final Logger logger = LoggerFactory.getLogger(APIConfig.class);
    private final ModuleContext context;
    private final State state;

    APIConfig(ModuleContext context) {
        this.context = context;
        state = context.config.state("api", State::new);
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
                String action = "api:" + ASCII.toLowerCase(httpMethod.name()) + ":" + path;
                context.httpServer.handler.route.add(httpMethod, path, new ControllerHolder(controller, targetMethod, controllerInfo, action, false));
            } catch (NoSuchMethodException e) {
                throw new Error("failed to find impl method", e);
            }
        }

        apiController().serviceInterfaces.add(serviceInterface);
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

    public void timeout(Duration timeout) {
        if (state.httpClient != null) throw new Error("api().timeout() must be configured before adding client");
        state.timeout = timeout;
    }

    public void slowOperationThreshold(Duration slowOperationThreshold) {
        if (state.httpClient != null) throw new Error("api().slowOperationThreshold() must be configured before adding client");
        state.slowOperationThreshold = slowOperationThreshold;
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
            HTTPClient httpClient = new HTTPClientBuilder()
                    .userAgent("APIClient")
                    .timeout(state.timeout)
                    .slowOperationThreshold(state.slowOperationThreshold)
                    .build();
            context.shutdownHook.add(httpClient::close);
            state.httpClient = httpClient;
        }
        return state.httpClient;
    }

    private APIController apiController() {
        if (state.apiController == null) {
            state.apiController = new APIController();
            context.route(HTTPMethod.GET, "/_sys/api", state.apiController, true);
        }
        return state.apiController;
    }

    public static class State {
        HTTPClient httpClient;
        Duration timeout = Duration.ofSeconds(30);
        Duration slowOperationThreshold = Duration.ofSeconds(15);
        APIController apiController;
    }
}
