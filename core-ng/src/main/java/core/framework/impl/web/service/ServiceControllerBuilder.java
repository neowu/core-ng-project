package core.framework.impl.web.service;

import core.framework.api.http.HTTPStatus;
import core.framework.api.util.Lists;
import core.framework.api.web.Controller;
import core.framework.api.web.Request;
import core.framework.api.web.Response;
import core.framework.api.web.service.PathParam;
import core.framework.api.web.service.ResponseStatus;
import core.framework.impl.code.CodeBuilder;
import core.framework.impl.code.DynamicInstanceBuilder;
import core.framework.impl.reflect.GenericTypes;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

import static core.framework.impl.code.CodeBuilder.typeVariableLiteral;

/**
 * @author neo
 */
public class ServiceControllerBuilder<T> {
    final Class<T> serviceInterface;
    final T service;
    final Method method;
    final HTTPStatus responseStatus;

    public ServiceControllerBuilder(Class<T> serviceInterface, T service, Method method) {
        this.serviceInterface = serviceInterface;
        this.service = service;
        this.method = method;

        ResponseStatus status = method.getDeclaredAnnotation(ResponseStatus.class);
        if (status != null) responseStatus = status.value();
        else responseStatus = HTTPStatus.OK;
    }

    public Controller build() {
        DynamicInstanceBuilder<Controller> builder = new DynamicInstanceBuilder<>(Controller.class, service.getClass().getCanonicalName() + "$" + method.getName());

        builder.addField(new CodeBuilder().append("final {} delegate;", serviceInterface.getCanonicalName()).build());
        builder.constructor(new Class[]{serviceInterface}, "this.delegate = $1;");
        builder.addMethod(buildMethod());

        return builder.build(service);
    }

    private String buildMethod() {
        CodeBuilder builder = new CodeBuilder();
        builder.append("public {} execute({} request) throws Exception {\n", Response.class.getCanonicalName(), Request.class.getCanonicalName());
        List<String> params = Lists.newArrayList();

        Annotation[][] annotations = method.getParameterAnnotations();
        Type[] paramTypes = method.getGenericParameterTypes();
        for (int i = 0, annotationsLength = annotations.length; i < annotationsLength; i++) {
            Type paramType = paramTypes[i];
            String paramTypeLiteral = GenericTypes.rawClass(paramType).getCanonicalName();
            PathParam pathParam = pathParam(annotations[i]);
            if (pathParam != null) {
                params.add(pathParam.value());
                builder.indent(1).append("{} {} = ({}) request.pathParam(\"{}\", {});\n",
                    paramTypeLiteral,
                    pathParam.value(),
                    paramTypeLiteral,
                    pathParam.value(),
                    typeVariableLiteral(paramType));
            } else {
                params.add("bean");
                builder.indent(1).append("{} bean = ({}) request.bean({});\n",
                    paramTypeLiteral,
                    paramTypeLiteral,
                    typeVariableLiteral(paramType));
            }
        }

        if (void.class == method.getReturnType()) {
            builder.indent(1).append("delegate.{}(", method.getName());
        } else {
            builder.indent(1).append("{} response = delegate.{}(", method.getReturnType().getCanonicalName(), method.getName());
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
                HTTPStatus.class.getCanonicalName() + "." + responseStatus.name());
        } else {
            builder.indent(1).append("return {}.bean(response).status({});\n",
                Response.class.getCanonicalName(),
                HTTPStatus.class.getCanonicalName() + "." + responseStatus.name());
        }

        builder.append("}");
        return builder.build();
    }

    private PathParam pathParam(Annotation[] annotations) {
        if (annotations.length == 0) return null;
        for (Annotation annotation : annotations) {
            if (annotation instanceof PathParam) return (PathParam) annotation;
        }
        return null;
    }
}
