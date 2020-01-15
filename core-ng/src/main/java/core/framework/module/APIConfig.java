package core.framework.module;

import core.framework.api.web.service.Path;
import core.framework.http.HTTPClient;
import core.framework.http.HTTPClientBuilder;
import core.framework.http.HTTPMethod;
import core.framework.internal.module.Config;
import core.framework.internal.module.ModuleContext;
import core.framework.internal.web.bean.RequestBeanMapper;
import core.framework.internal.web.bean.ResponseBeanMapper;
import core.framework.internal.web.controller.ControllerHolder;
import core.framework.internal.web.service.HTTPMethods;
import core.framework.internal.web.service.WebServiceClient;
import core.framework.internal.web.service.WebServiceClientBuilder;
import core.framework.internal.web.service.WebServiceControllerBuilder;
import core.framework.internal.web.service.WebServiceImplValidator;
import core.framework.internal.web.service.WebServiceInterfaceValidator;
import core.framework.util.ASCII;
import core.framework.web.Controller;
import core.framework.web.service.WebServiceClientProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.time.Duration;

/**
 * @author neo
 */
public class APIConfig extends Config {
    private final Logger logger = LoggerFactory.getLogger(APIConfig.class);
    ModuleContext context;
    private HTTPClientBuilder httpClientBuilder;
    private HTTPClient httpClient;

    @Override
    protected void initialize(ModuleContext context, String name) {
        this.context = context;
        // default value is for internal api call only, targeting for kube env (with short connect timeout and more retries)
        httpClientBuilder = new HTTPClientBuilder()
                .userAgent(WebServiceClient.USER_AGENT)
                .trustAll()
                .connectTimeout(Duration.ofSeconds(2))
                .timeout(Duration.ofSeconds(20))    // refer to: kube graceful shutdown period is 30s, db timeout is 15s
                .slowOperationThreshold(Duration.ofSeconds(10))
                .maxRetries(5);
    }

    @Override
    protected void validate() {
        httpClientBuilder = null;
    }

    public <T> void service(Class<T> serviceInterface, T service) {
        logger.info("create web service, interface={}", serviceInterface.getCanonicalName());
        new WebServiceInterfaceValidator(serviceInterface,
                context.httpServer.handler.requestBeanMapper,
                context.httpServer.handler.responseBeanMapper,
                context.serviceRegistry.beanClassNameValidator).validate();
        new WebServiceImplValidator<>(serviceInterface, service).validate();
        context.serviceRegistry.serviceInterfaces.add(serviceInterface);    // doesn't need to check duplicate, duplication will failed to register route

        for (Method method : serviceInterface.getMethods()) {
            HTTPMethod httpMethod = HTTPMethods.httpMethod(method);
            String path = method.getDeclaredAnnotation(Path.class).value();
            Controller controller = new WebServiceControllerBuilder<>(serviceInterface, service, method).build();
            try {
                Class<?> serviceClass = service.getClass();
                Method targetMethod = serviceClass.getMethod(method.getName(), method.getParameterTypes());
                String controllerInfo = serviceClass.getCanonicalName() + "." + targetMethod.getName();
                String action = "api:" + ASCII.toLowerCase(httpMethod.name()) + ":" + path;
                context.httpServer.handler.route.add(httpMethod, path, new ControllerHolder(controller, targetMethod, controllerInfo, action, false));
            } catch (NoSuchMethodException e) {
                throw new Error("failed to find impl method", e);
            }
        }
    }

    public <T> APIClientConfig client(Class<T> serviceInterface, String serviceURL) {
        T client = createClient(serviceInterface, serviceURL);
        context.beanFactory.bind(serviceInterface, null, client);
        return new APIClientConfig((WebServiceClientProxy) client);
    }

    public <T> T createClient(Class<T> serviceInterface, String serviceURL) {
        logger.info("create web service client, interface={}, serviceURL={}", serviceInterface.getCanonicalName(), serviceURL);
        RequestBeanMapper requestBeanMapper = context.httpServer.handler.requestBeanMapper;
        ResponseBeanMapper responseBeanMapper = context.httpServer.handler.responseBeanMapper;
        new WebServiceInterfaceValidator(serviceInterface, requestBeanMapper, responseBeanMapper, context.serviceRegistry.beanClassNameValidator).validate();

        HTTPClient httpClient = getOrCreateHTTPClient();
        var webServiceClient = new WebServiceClient(serviceURL, httpClient, requestBeanMapper, responseBeanMapper);
        return createWebServiceClient(serviceInterface, webServiceClient);
    }

    <T> T createWebServiceClient(Class<T> serviceInterface, WebServiceClient webServiceClient) {
        return new WebServiceClientBuilder<>(serviceInterface, webServiceClient).build();
    }

    public HTTPClientBuilder httpClient() {
        if (httpClient != null) throw new Error("api().httpClient() must be configured before adding client");
        return httpClientBuilder;
    }

    private HTTPClient getOrCreateHTTPClient() {
        if (httpClient == null) {
            this.httpClient = httpClientBuilder.build();
        }
        return httpClient;
    }
}
