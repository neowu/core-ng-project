package core.framework.module;

import core.framework.api.web.service.Path;
import core.framework.http.HTTPClient;
import core.framework.http.HTTPClientBuilder;
import core.framework.http.HTTPMethod;
import core.framework.impl.module.Config;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.web.ControllerHolder;
import core.framework.impl.web.bean.RequestBeanMapper;
import core.framework.impl.web.service.HTTPMethods;
import core.framework.impl.web.service.WebServiceClient;
import core.framework.impl.web.service.WebServiceClientBuilder;
import core.framework.impl.web.service.WebServiceControllerBuilder;
import core.framework.impl.web.service.WebServiceImplValidator;
import core.framework.impl.web.service.WebServiceInterfaceValidator;
import core.framework.util.ASCII;
import core.framework.util.Lists;
import core.framework.web.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.List;

/**
 * @author neo
 */
public class APIConfig extends Config {
    final List<Class<?>> serviceInterfaces = Lists.newArrayList();
    private final Logger logger = LoggerFactory.getLogger(APIConfig.class);
    private ModuleContext context;
    private HTTPClient httpClient;
    private Duration timeout = Duration.ofSeconds(30);
    private Duration slowOperationThreshold = Duration.ofSeconds(15);

    @Override
    protected void initialize(ModuleContext context, String name) {
        this.context = context;
    }

    @Override
    protected void validate() {
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

        serviceInterfaces.add(serviceInterface);
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
        if (httpClient != null) throw new Error("api timeout must be configured before adding client");
        this.timeout = timeout;
    }

    public void slowOperationThreshold(Duration slowOperationThreshold) {
        if (httpClient != null) throw new Error("api slowOperationThreshold must be configured before adding client");
        this.slowOperationThreshold = slowOperationThreshold;
    }

    <T> T createWebServiceClient(Class<T> serviceInterface, WebServiceClient webServiceClient) {
        return new WebServiceClientBuilder<>(serviceInterface, webServiceClient).build();
    }

    private HTTPClient httpClient() {
        if (httpClient == null) {
            HTTPClient httpClient = new HTTPClientBuilder()
                    .userAgent("APIClient")
                    .timeout(timeout)
                    .slowOperationThreshold(slowOperationThreshold)
                    .build();
            context.shutdownHook.add(httpClient::close);
            this.httpClient = httpClient;
        }
        return httpClient;
    }
}
