package core.framework.module;

import core.framework.api.web.service.Path;
import core.framework.http.HTTPClient;
import core.framework.http.HTTPClientBuilder;
import core.framework.http.HTTPMethod;
import core.framework.impl.module.Config;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.reflect.Classes;
import core.framework.impl.web.bean.BeanMapperRegistry;
import core.framework.impl.web.bean.RequestBeanMapper;
import core.framework.impl.web.bean.ResponseBeanMapper;
import core.framework.impl.web.controller.ControllerHolder;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public class APIConfig extends Config {
    final Map<String, Class<?>> serviceInterfaces = new HashMap<>();
    final Set<Class<?>> beanClasses = new HashSet<>();     // extra beans not defined in service interfaces, e.g. web socket json, raw controller request/response
    private final Logger logger = LoggerFactory.getLogger(APIConfig.class);
    private ModuleContext context;
    private HTTPClientBuilder httpClientBuilder;
    private HTTPClient httpClient;

    @Override
    protected void initialize(ModuleContext context, String name) {
        this.context = context;
        httpClientBuilder = new HTTPClientBuilder()
                .userAgent(WebServiceClient.USER_AGENT)
                .trustAll()
                .timeout(Duration.ofSeconds(15))    // kube graceful shutdown period is 30s, we need to finish api call within that time
                .maxRetries(3)
                .slowOperationThreshold(Duration.ofSeconds(10));
    }

    public <T> void service(Class<T> serviceInterface, T service) {
        if (!beanClasses.isEmpty()) throw new Error("api().service() must be configured before api().bean()");

        logger.info("create web service, interface={}", serviceInterface.getCanonicalName());
        new WebServiceInterfaceValidator(serviceInterface,
                context.httpServer.handler.requestBeanMapper,
                context.httpServer.handler.responseBeanMapper).validate();
        new WebServiceImplValidator<>(serviceInterface, service).validate();

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

        Class<?> previous = serviceInterfaces.putIfAbsent(Classes.className(serviceInterface), serviceInterface);
        if (previous != null) throw new Error(format("found service interface with duplicate name which can be confusing, please use different class name, previousClass={}, class={}", previous.getCanonicalName(), serviceInterface.getCanonicalName()));
    }

    public <T> APIClientConfig client(Class<T> serviceInterface, String serviceURL) {
        logger.info("create web service client, interface={}, serviceURL={}", serviceInterface.getCanonicalName(), serviceURL);
        RequestBeanMapper requestBeanMapper = context.httpServer.handler.requestBeanMapper;
        ResponseBeanMapper responseBeanMapper = context.httpServer.handler.responseBeanMapper;
        new WebServiceInterfaceValidator(serviceInterface, requestBeanMapper, responseBeanMapper).validate();

        HTTPClient httpClient = getOrCreateHTTPClient();
        var webServiceClient = new WebServiceClient(serviceURL, httpClient, requestBeanMapper, responseBeanMapper);
        T client = createWebServiceClient(serviceInterface, webServiceClient);
        context.beanFactory.bind(serviceInterface, null, client);
        return new APIClientConfig(webServiceClient);
    }

    <T> T createWebServiceClient(Class<T> serviceInterface, WebServiceClient webServiceClient) {
        return new WebServiceClientBuilder<>(serviceInterface, webServiceClient).build();
    }

    public HTTPClientBuilder httpClient() {
        if (httpClient != null) throw new Error("http client must be configured before adding client");
        return httpClientBuilder;
    }

    private HTTPClient getOrCreateHTTPClient() {
        if (httpClient == null) {
            this.httpClient = httpClientBuilder.build();
        }
        return httpClient;
    }

    public void bean(Class<?>... beanClasses) {
        BeanMapperRegistry registry = context.httpServer.handler.beanMapperRegistry;
        for (Class<?> beanClass : beanClasses) {
            if (registry.beanMappers.containsKey(beanClass)) {
                throw new Error(format("bean class is already registered or referred by service interface, class={}", beanClass.getCanonicalName()));
            }
            registry.register(beanClass);
            this.beanClasses.add(beanClass);
        }
    }
}
