package core.framework.impl.web.service;

import core.framework.api.web.service.Path;
import core.framework.api.web.service.PathParam;
import core.framework.http.HTTPMethod;
import core.framework.impl.asm.CodeBuilder;
import core.framework.impl.asm.DynamicInstanceBuilder;
import core.framework.impl.reflect.Params;
import core.framework.util.Maps;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

import static core.framework.impl.asm.Literal.type;
import static core.framework.impl.asm.Literal.variable;

/**
 * @author neo
 */
public class WebServiceClientBuilder<T> {
    final DynamicInstanceBuilder<T> builder;
    private final Class<T> serviceInterface;
    private final WebServiceClient client;

    public WebServiceClientBuilder(Class<T> serviceInterface, WebServiceClient client) {
        this.serviceInterface = serviceInterface;
        this.client = client;
        builder = new DynamicInstanceBuilder<>(serviceInterface, serviceInterface.getCanonicalName() + "$Client");
    }

    public T build() {
        builder.addField("private final {} client;", type(WebServiceClient.class));
        builder.constructor(new Class<?>[]{WebServiceClient.class}, "this.client = $1;");

        Method[] methods = serviceInterface.getMethods();
        Arrays.sort(methods, Comparator.comparing(Method::getName));    // to make generated code deterministic
        for (Method method : methods) {
            builder.addMethod(buildMethod(method));
        }

        return builder.build(client);
    }

    private String buildMethod(Method method) {
        CodeBuilder builder = new CodeBuilder();

        Type returnType = method.getGenericReturnType();

        Map<String, Integer> pathParamIndexes = Maps.newHashMap();
        Type requestBeanType = null;
        Integer requestBeanIndex = null;
        builder.append("public {} {}(", type(returnType), method.getName());
        Annotation[][] annotations = method.getParameterAnnotations();
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> paramClass = parameterTypes[i];
            if (i > 0) builder.append(", ");
            builder.append("{} param{}", type(paramClass), i);

            PathParam pathParam = Params.annotation(annotations[i], PathParam.class);
            if (pathParam != null) {
                pathParamIndexes.put(pathParam.value(), i);
            } else {
                requestBeanIndex = i;
                requestBeanType = method.getGenericParameterTypes()[i];
            }
        }
        builder.append(") {\n");

        builder.indent(1).append("java.lang.reflect.Type requestType = {};\n", requestBeanType == null ? "null" : variable(requestBeanType));
        builder.indent(1).append("Object requestBean = {};\n", requestBeanIndex == null ? "null" : "param" + requestBeanIndex);

        builder.indent(1).append("java.util.Map pathParams = new java.util.HashMap();\n");
        pathParamIndexes.forEach((name, index) ->
                builder.indent(1).append("pathParams.put({}, param{});\n", variable(name), index));

        String returnTypeLiteral = returnType == void.class ? type(Void.class) : type(returnType);

        String path = method.getDeclaredAnnotation(Path.class).value();
        builder.indent(1).append("String serviceURL = client.serviceURL({}, pathParams);\n", variable(path)); // to pass path as string literal, the escaped char will not be transferred, like \\, currently not convert is because only type regex may contain special char

        HTTPMethod httpMethod = HTTPMethods.httpMethod(method);
        builder.indent(1).append("{} response = ({}) client.execute({}, serviceURL, requestType, requestBean, {});\n",
                returnTypeLiteral,
                returnTypeLiteral,
                variable(httpMethod),
                variable(returnType));

        if (returnType != void.class) builder.indent(1).append("return response;\n");

        builder.append("}");
        return builder.build();
    }
}
