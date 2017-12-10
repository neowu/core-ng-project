package core.framework.impl.web.api;

import core.framework.api.http.HTTPStatus;
import core.framework.api.json.Property;
import core.framework.api.validate.Length;
import core.framework.api.validate.Max;
import core.framework.api.validate.Min;
import core.framework.api.validate.NotEmpty;
import core.framework.api.validate.NotNull;
import core.framework.api.validate.Pattern;
import core.framework.api.validate.Size;
import core.framework.api.web.service.Path;
import core.framework.api.web.service.PathParam;
import core.framework.api.web.service.QueryParam;
import core.framework.api.web.service.ResponseStatus;
import core.framework.http.HTTPMethod;
import core.framework.impl.reflect.Classes;
import core.framework.impl.reflect.GenericTypes;
import core.framework.impl.reflect.Params;
import core.framework.impl.web.service.HTTPMethods;
import core.framework.util.ASCII;
import core.framework.util.Lists;
import core.framework.util.Strings;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author neo
 */
class OpenAPIDocumentBuilder {
    final JSONNode document = new JSONNode();

    OpenAPIDocumentBuilder() {
        document.put("openapi", "3.0.0");
        document.get("info").put("version", "v1.0");
    }

    void title(String title) {
        document.get("info").put("title", title);
    }

    void addServiceInterface(Class<?> serviceInterface) {
        Method[] methods = serviceInterface.getMethods();
        Arrays.sort(methods, Comparator.comparing((Method method) -> method.getDeclaredAnnotation(Path.class).value()).thenComparing(method -> HTTPMethods.httpMethod(method).ordinal()));
        for (Method method : methods) {
            HTTPMethod httpMethod = HTTPMethods.httpMethod(method);
            JSONNode path = document.get("paths").get(path(method));
            path.put("description", method.getName());
            JSONNode operation = buildOperation(method);
            operation.put("tags", Lists.newArrayList(serviceInterface.getCanonicalName()));
            path.put(ASCII.toLowerCase(httpMethod.name()), operation);
        }
    }

    private JSONNode buildOperation(Method method) {
        JSONNode operation = new JSONNode();
        operation.put("description", method.getName());

        buildParameters(operation, method);
        buildResponse(operation, method);

        return operation;
    }

    private void buildResponse(JSONNode operation, Method method) {
        Type responseType = method.getGenericReturnType();
        HTTPStatus status = responseStatus(method);

        JSONNode response = operation.get("responses").get(String.valueOf(status.code));
        response.put("description", String.valueOf(status));

        JSONNode responseSchema = buildSchema(responseType);
        if (responseSchema != null)
            response.get("content").get("application/json").put("schema", responseSchema);
    }

    private void buildParameters(JSONNode operation, Method method) {
        HTTPMethod httpMethod = HTTPMethods.httpMethod(method);

        Annotation[][] annotations = method.getParameterAnnotations();
        Type[] paramTypes = method.getGenericParameterTypes();

        for (int i = 0; i < paramTypes.length; i++) {
            Type paramType = paramTypes[i];
            PathParam pathParam = Params.annotation(annotations[i], PathParam.class);
            if (pathParam != null) {
                buildPathParam(operation, paramType, pathParam);
            } else {
                if (httpMethod == HTTPMethod.GET || httpMethod == HTTPMethod.DELETE) {
                    buildQueryParam(operation, paramType);
                } else {
                    operation.get("requestBody").get("content").get("application/json").put("schema", buildSchema(paramType));
                }
            }
        }
    }

    private void buildQueryParam(JSONNode operation, Type paramType) {
        Class<?> paramClass = GenericTypes.rawClass(paramType);
        for (Field field : paramClass.getFields()) {
            JSONNode parameter = new JSONNode();
            String queryParam = field.getDeclaredAnnotation(QueryParam.class).name();
            parameter.put("name", queryParam);
            parameter.put("in", "query");
            if (field.isAnnotationPresent(NotNull.class)) {
                parameter.put("required", true);
            }
            JSONNode schema = buildSchema(field.getGenericType());
            buildValidation(schema, field);
            parameter.put("schema", schema);
            parameter.put("description", field.getName());
            operation.add("parameters", parameter);
        }
    }

    private void buildPathParam(JSONNode operation, Type paramType, PathParam pathParam) {
        JSONNode parameter = new JSONNode();
        parameter.put("name", pathParam.value());
        parameter.put("in", "path");
        parameter.put("required", true);
        parameter.put("schema", buildSchema(paramType));
        parameter.put("description", pathParam.value());
        operation.add("parameters", parameter);
    }

    private <T extends Enum<T>> JSONNode buildSchema(Type type) {
        if (type == void.class) return null;
        Class<?> instanceClass = GenericTypes.rawClass(type);
        JSONNode schema = new JSONNode();
        if (GenericTypes.isOptional(type)) {
            schema.put("$ref", buildObjectSchema(GenericTypes.optionalValueClass(type)));
        } else if (GenericTypes.isList(type)) {
            Class<?> itemClass = GenericTypes.listValueClass(type);
            schema.put("type", "array");
            schema.put("items", buildSchema(itemClass));
        } else if (GenericTypes.isMap(type)) {
            schema.put("type", "object");
            schema.put("additionalProperties", buildSchema(GenericTypes.mapValueClass(type)));
        } else if (String.class.equals(instanceClass)) {
            schema.put("type", "string");
        } else if (Integer.class.equals(instanceClass)) {
            schema.put("type", "integer");
            schema.put("format", "int32");
        } else if (Boolean.class.equals(instanceClass)) {
            schema.put("type", "boolean");
        } else if (Long.class.equals(instanceClass)) {
            schema.put("type", "integer");
            schema.put("format", "int64");
        } else if (Double.class.equals(instanceClass) || BigDecimal.class.equals(instanceClass)) {
            schema.put("type", "number");
            schema.put("format", "double");
        } else if (LocalDate.class.equals(instanceClass)) {
            schema.put("type", "string");
            schema.put("format", "date");
        } else if (LocalDateTime.class.equals(instanceClass) || ZonedDateTime.class.equals(instanceClass) || Instant.class.equals(instanceClass)) {
            schema.put("type", "string");
            schema.put("format", "date-time");
        } else if (instanceClass.isEnum()) {
            schema.put("type", "string");
            @SuppressWarnings("unchecked")
            List<String> enumValues = enumValues((Class<T>) instanceClass);
            enumValues.forEach(enumValue -> schema.add("enum", enumValue));
        } else {
            schema.put("$ref", buildObjectSchema(instanceClass));
        }
        return schema;
    }

    private String buildObjectSchema(Class<?> objectClass) {
        String schemaName = objectClass.getCanonicalName();
        String ref = "#/components/schemas/" + schemaName;
        JSONNode schemas = document.get("components").get("schemas");
        if (schemas.has(schemaName)) return ref;

        JSONNode schema = schemas.get(schemaName);
        schema.put("type", "object");
        for (Field field : objectClass.getFields()) {
            String property = field.getDeclaredAnnotation(Property.class).name();
            if (field.isAnnotationPresent(NotNull.class)) schema.add("required", property);
            JSONNode fieldSchema = buildSchema(field.getGenericType());
            buildValidation(fieldSchema, field);
            schema.get("properties").put(property, fieldSchema);
        }
        return ref;
    }

    private void buildValidation(JSONNode schema, Field field) {
        Min min = field.getDeclaredAnnotation(Min.class);
        if (min != null) schema.put("minimum", min.value());
        Max max = field.getDeclaredAnnotation(Max.class);
        if (max != null) schema.put("maximum", max.value());
        Pattern pattern = field.getDeclaredAnnotation(Pattern.class);
        if (pattern != null) schema.put("pattern", pattern.value());
        NotEmpty notEmpty = field.getDeclaredAnnotation(NotEmpty.class);
        if (notEmpty != null) schema.put("minLength", 1);   // there is no not empty validation in openapi, here to use closest one
        Length length = field.getDeclaredAnnotation(Length.class);
        if (length != null) {
            if (length.min() >= 0) schema.put("minLength", length.min());
            if (length.max() >= 0) schema.put("maxLength", length.max());
        }
        Size size = field.getDeclaredAnnotation(Size.class);
        if (size != null) {
            if (size.min() >= 0) schema.put("minItems", size.min());
            if (size.max() >= 0) schema.put("maxItems", size.max());
        }
    }

    private <T extends Enum<T>> List<String> enumValues(Class<T> enumClass) {
        return Arrays.stream(enumClass.getEnumConstants())
                     .map(constant -> Classes.enumValueAnnotation(enumClass, constant, Property.class).name())
                     .collect(Collectors.toList());
    }

    private String path(Method method) {
        String path = method.getDeclaredAnnotation(Path.class).value();
        String[] tokens = Strings.split(path, '/');
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            if (token.length() == 0 || i > 1) builder.append('/');
            if (token.startsWith(":")) {
                builder.append('{').append(token.substring(1)).append('}');
            } else {
                builder.append(token);
            }
        }
        return builder.toString();
    }

    private HTTPStatus responseStatus(Method method) {
        ResponseStatus status = method.getDeclaredAnnotation(ResponseStatus.class);
        if (status == null) return HTTPStatus.OK;
        return status.value();
    }
}
