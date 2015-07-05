package core.framework.api.module;

import core.framework.api.http.HTTPClient;
import core.framework.api.http.HTTPClientBuilder;
import core.framework.api.http.HTTPMethod;
import core.framework.api.web.Controller;
import core.framework.api.web.service.Path;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.web.BeanValidator;
import core.framework.impl.web.client.WebServiceClient;
import core.framework.impl.web.client.WebServiceClientBuilder;
import core.framework.impl.web.service.HTTPMethodHelper;
import core.framework.impl.web.service.ServiceControllerBuilder;
import core.framework.impl.web.service.ServiceInterfaceValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * @author neo
 */
public class APIConfig {
    private final Logger logger = LoggerFactory.getLogger(APIConfig.class);
    private final ModuleContext context;

    public APIConfig(ModuleContext context) {
        this.context = context;
    }

    public <T> void service(Class<T> serviceInterface, T service) {
        logger.info("register service interface, interface={}", serviceInterface.getCanonicalName());
        BeanValidator validator = context.httpServer.validator;
        new ServiceInterfaceValidator(serviceInterface, validator).validate();

        Method[] methods = serviceInterface.getDeclaredMethods();
        for (Method method : methods) {
            HTTPMethod httpMethod = HTTPMethodHelper.httpMethod(method);
            String path = method.getDeclaredAnnotation(Path.class).value();
            Controller controller = new ServiceControllerBuilder<>(serviceInterface, service, method).build();
            context.httpServer.add(httpMethod, path, controller);
        }
    }

    public <T> WebServiceClientConfig client(Class<T> serviceInterface, String serviceURL) {
        logger.info("register service client, interface={}", serviceInterface.getCanonicalName());
        HTTPClient httpClient = httpClient();
        BeanValidator validator = new BeanValidator();
        new ServiceInterfaceValidator(serviceInterface, validator).validate();

        WebServiceClient webServiceClient = new WebServiceClient(serviceURL, httpClient, validator);

        T client = new WebServiceClientBuilder<>(serviceInterface, webServiceClient).build();
        context.beanFactory.bind(serviceInterface, null, client);

        return webServiceClient;
    }

    private HTTPClient httpClient() {
        if (context.beanFactory.registered(HTTPClient.class, null))
            return context.beanFactory.bean(HTTPClient.class, null);

        HTTPClient httpClient = new HTTPClientBuilder().build();
        context.beanFactory.bind(HTTPClient.class, null, httpClient);
        context.shutdownHook.add(httpClient::shutdown);
        return httpClient;
    }
}
