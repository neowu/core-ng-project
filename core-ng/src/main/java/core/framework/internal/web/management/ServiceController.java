package core.framework.internal.web.management;

import core.framework.internal.log.LogManager;
import core.framework.internal.module.ServiceRegistry;
import core.framework.internal.reflect.Classes;
import core.framework.internal.web.http.IPv4AccessControl;
import core.framework.web.Controller;
import core.framework.web.Request;
import core.framework.web.Response;

import java.util.stream.Collectors;

/**
 * @author neo
 */
public class ServiceController implements Controller {
    private final IPv4AccessControl accessControl = new IPv4AccessControl();
    private final ServiceRegistry registry;

    public ServiceController(ServiceRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Response execute(Request request) {
        accessControl.validate(request.clientIP());

        ServiceResponse response = serviceResponse();
        return Response.bean(response);
    }

    ServiceResponse serviceResponse() {
        var response = new ServiceResponse();
        response.app = LogManager.APP_NAME;
        response.services = registry.serviceInterfaces.stream().map(Classes::className).collect(Collectors.toList());
        response.clients = registry.clientInterfaces.stream().map(Classes::className).collect(Collectors.toList());
        response.producers = registry.producerMessageClasses.stream().map(Classes::className).collect(Collectors.toList());
        response.consumers = registry.consumerMessageClasses.stream().map(Classes::className).collect(Collectors.toList());
        return response;
    }
}
