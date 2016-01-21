package core.framework.impl.web.service;

import core.framework.api.http.HTTPMethod;
import core.framework.api.util.Maps;
import core.framework.api.web.service.Path;
import core.framework.api.web.service.PathParam;
import core.framework.impl.code.CodeBuilder;
import core.framework.impl.code.DynamicInstanceBuilder;
import core.framework.impl.reflect.GenericTypes;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

import static core.framework.impl.code.CodeBuilder.enumVariableLiteral;
import static core.framework.impl.code.CodeBuilder.typeVariableLiteral;

/**
 * @author neo
 */
public class WebServiceClientBuilder<T> {
    private final Class<T> serviceInterface;
    private final WebServiceClient client;

    public WebServiceClientBuilder(Class<T> serviceInterface, WebServiceClient client) {
        this.serviceInterface = serviceInterface;
        this.client = client;
    }

    public T build() {
        DynamicInstanceBuilder<T> builder = new DynamicInstanceBuilder<>(serviceInterface, serviceInterface.getCanonicalName() + "$Client");

        builder.addField(new CodeBuilder().append("final {} client;", WebServiceClient.class.getCanonicalName()).build());
        builder.constructor(new Class[]{WebServiceClient.class}, "this.client = $1;");

        for (Method method : serviceInterface.getMethods()) {
            builder.addMethod(buildMethod(method));
        }

        return builder.build(client);
    }

    private String buildMethod(Method method) {
        CodeBuilder builder = new CodeBuilder();

        String path = method.getDeclaredAnnotation(Path.class).value();
        HTTPMethod httpMethod = HTTPMethodHelper.httpMethod(method);

        Type returnType = method.getGenericReturnType();

        Map<String, Integer> pathParamIndexes = Maps.newHashMap();
        Type requestBeanType = null;
        Integer requestBeanIndex = null;
        builder.append("public {} {}(", GenericTypes.rawClass(returnType).getCanonicalName(), method.getName());
        Annotation[][] annotations = method.getParameterAnnotations();
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> paramClass = parameterTypes[i];
            if (i > 0) builder.append(" ,");
            builder.append("{} param{}", paramClass.getCanonicalName(), i);

            PathParam pathParam = pathParam(annotations[i]);
            if (pathParam != null) {
                pathParamIndexes.put(pathParam.value(), i);
            } else {
                requestBeanIndex = i;
                requestBeanType = method.getGenericParameterTypes()[i];
            }
        }
        builder.append(") {\n");

        builder.indent(1).append("java.lang.reflect.Type requestType = {};\n", requestBeanType == null ? "null" : typeVariableLiteral(requestBeanType));
        builder.indent(1).append("Object requestBean = {};\n", requestBeanIndex == null ? "null" : "param" + requestBeanIndex);

        builder.indent(1).append("java.util.Map pathParams = new java.util.HashMap();\n");
        pathParamIndexes.forEach((name, index) ->
            builder.indent(1).append("pathParams.put(\"{}\", param{});\n", name, index));

        String returnTypeLiteral = returnType == void.class ? Void.class.getCanonicalName() : GenericTypes.rawClass(returnType).getCanonicalName();

        builder.indent(1).append("String serviceURL = client.serviceURL(\"{}\", pathParams);\n", path); // to pass path as string literal, the escaped char will not be transferred, like \\, currently not convert is because only type regex may contain special char

        builder.indent(1).append("{} response = ({}) client.execute({}, serviceURL, requestType, requestBean, {});\n",
            returnTypeLiteral,
            returnTypeLiteral,
            enumVariableLiteral(httpMethod),
            typeVariableLiteral(returnType));

        if (returnType != void.class) builder.indent(1).append("return response;\n");

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
