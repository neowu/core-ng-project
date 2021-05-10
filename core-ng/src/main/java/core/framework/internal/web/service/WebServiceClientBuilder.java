package core.framework.internal.web.service;

import core.framework.api.web.service.Path;
import core.framework.api.web.service.PathParam;
import core.framework.internal.asm.CodeBuilder;
import core.framework.internal.asm.DynamicInstanceBuilder;
import core.framework.internal.reflect.Methods;
import core.framework.internal.reflect.Params;
import core.framework.util.Maps;
import core.framework.web.service.WebServiceClientInterceptor;
import core.framework.web.service.WebServiceClientProxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

import static core.framework.internal.asm.Literal.type;
import static core.framework.internal.asm.Literal.variable;

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
        builder = new DynamicInstanceBuilder<>(serviceInterface, "Client");
    }

    public T build() {
        builder.addField("private final {} client;", type(WebServiceClient.class));
        builder.constructor(new Class<?>[]{WebServiceClient.class}, "this.client = $1;");

        Method[] methods = serviceInterface.getMethods();
        Arrays.sort(methods, Comparator.comparing(Method::getName));    // to make generated code deterministic
        for (Method method : methods) {
            builder.addMethod(buildImplMethod(method));
        }

        // all client will impl proxy interface to additional configuration
        builder.addInterface(WebServiceClientProxy.class);
        builder.addMethod(buildInterceptMethod());

        return builder.build(client);
    }

    private String buildInterceptMethod() {
        var builder = new CodeBuilder();
        builder.append("public void intercept({} interceptor) {\n", type(WebServiceClientInterceptor.class))
            .indent(1).append("client.intercept(interceptor);\n")
            .append("}");
        return builder.build();
    }

    private String buildImplMethod(Method method) {
        var builder = new CodeBuilder();

        Type returnType = method.getGenericReturnType();
        Class<?> returnClass = method.getReturnType();

        Map<String, Integer> pathParamIndexes = Maps.newHashMap();
        Class<?> requestBeanClass = null;
        Integer requestBeanIndex = null;
        builder.append("public {} {}(", type(returnClass), method.getName());
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
                requestBeanClass = paramClass;
            }
        }
        builder.append(") {\n");
        builder.indent(1).append("client.logCallWebService({});\n", variable(Methods.path(method)));

        buildPath(builder, method.getDeclaredAnnotation(Path.class).value(), pathParamIndexes);
        builder.indent(1).append("Class requestBeanClass = {};\n", requestBeanClass == null ? "null" : variable(requestBeanClass));
        builder.indent(1).append("Object requestBean = {};\n", requestBeanIndex == null ? "null" : "param" + requestBeanIndex);

        builder.indent(1);
        if (returnType != void.class) builder.append("return ({}) ", type(returnClass));
        builder.append("client.execute({}, path, requestBeanClass, requestBean, {});\n", variable(HTTPMethods.httpMethod(method)), variable(returnType));

        builder.append("}");
        return builder.build();
    }

    void buildPath(CodeBuilder builder, String path, Map<String, Integer> pathParamIndexes) {
        if (pathParamIndexes.isEmpty()) {
            builder.indent(1).append("String path = {};\n", variable(path)); // to pass path as string literal, not escaping char, like \\ which is not allowed, refer to core.framework.internal.web.route.PathPatternValidator
        } else {
            for (Map.Entry<String, Integer> entry : pathParamIndexes.entrySet()) {
                builder.indent(1).append("if (param{} == null) throw new Error(\"path param must not be null, name={}\");\n", entry.getValue(), entry.getKey());
            }

            builder.indent(1).append("StringBuilder builder = new StringBuilder();\n");
            int currentIndex = 0;
            while (true) {
                int variableStart = path.indexOf(':', currentIndex);
                if (variableStart < 0) break;
                int variableEnd = path.indexOf('/', variableStart);
                if (variableEnd < 0) variableEnd = path.length();
                builder.indent(1).append("builder.append({}).append({}.toString(param{}));\n", variable(path.substring(currentIndex, variableStart)),
                    type(PathParamHelper.class), pathParamIndexes.get(path.substring(variableStart + 1, variableEnd)));
                currentIndex = variableEnd;
            }
            if (currentIndex < path.length()) {
                builder.indent(1).append("builder.append({});\n", variable(path.substring(currentIndex)));
            }
            builder.indent(1).append("String path = builder.toString();\n");
        }
    }
}
