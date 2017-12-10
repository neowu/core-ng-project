package core.framework.impl.web.service;

import core.framework.api.http.HTTPStatus;
import core.framework.api.web.service.PathParam;
import core.framework.api.web.service.ResponseStatus;
import core.framework.impl.asm.CodeBuilder;
import core.framework.impl.asm.DynamicInstanceBuilder;
import core.framework.impl.reflect.Params;
import core.framework.util.Lists;
import core.framework.web.Controller;
import core.framework.web.Request;
import core.framework.web.Response;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

import static core.framework.impl.asm.Literal.type;
import static core.framework.impl.asm.Literal.variable;

/**
 * @author neo
 */
public class WebServiceControllerBuilder<T> {
    final DynamicInstanceBuilder<Controller> builder;
    private final Class<T> serviceInterface;
    private final T service;
    private final Method method;
    private final HTTPStatus responseStatus;

    public WebServiceControllerBuilder(Class<T> serviceInterface, T service, Method method) {
        this.serviceInterface = serviceInterface;
        this.service = service;
        this.method = method;

        ResponseStatus status = method.getDeclaredAnnotation(ResponseStatus.class);
        if (status != null) responseStatus = status.value();
        else responseStatus = HTTPStatus.OK;

        builder = new DynamicInstanceBuilder<>(Controller.class, service.getClass().getCanonicalName() + "$" + method.getName());
    }

    public Controller build() {
        builder.addField("private final {} delegate;", type(serviceInterface));
        builder.constructor(new Class<?>[]{serviceInterface}, "this.delegate = $1;");
        builder.addMethod(buildMethod());
        return builder.build(service);
    }

    private String buildMethod() {
        CodeBuilder builder = new CodeBuilder();
        builder.append("public {} execute({} request) throws Exception {\n", type(Response.class), type(Request.class));
        List<String> params = Lists.newArrayList();

        Annotation[][] annotations = method.getParameterAnnotations();
        Type[] paramTypes = method.getGenericParameterTypes();
        for (int i = 0; i < annotations.length; i++) {
            Type paramType = paramTypes[i];
            String paramTypeLiteral = type(paramType);
            PathParam pathParam = Params.annotation(annotations[i], PathParam.class);
            if (pathParam != null) {
                params.add(pathParam.value());
                builder.indent(1).append("{} {} = ({}) request.pathParam(\"{}\", {});\n",
                        paramTypeLiteral,
                        pathParam.value(),
                        paramTypeLiteral,
                        pathParam.value(),
                        variable(paramType));
            } else {
                params.add("bean");
                builder.indent(1).append("{} bean = ({}) request.bean({});\n",
                        paramTypeLiteral,
                        paramTypeLiteral,
                        variable(paramType));
            }
        }

        if (void.class == method.getReturnType()) {
            builder.indent(1).append("delegate.{}(", method.getName());
        } else {
            builder.indent(1).append("{} response = delegate.{}(", type(method.getReturnType()), method.getName());
        }

        int index = 0;
        for (String param : params) {
            if (index > 0) builder.append(", ");
            builder.append(param);
            index++;
        }
        builder.append(");\n");

        if (void.class.equals(method.getReturnType())) {
            builder.indent(1).append("return {}.empty().status({});\n",
                    Response.class.getCanonicalName(),
                    variable(responseStatus));
        } else {
            builder.indent(1).append("return {}.bean(response).status({});\n",
                    Response.class.getCanonicalName(),
                    variable(responseStatus));
        }

        builder.append("}");
        return builder.build();
    }
}
