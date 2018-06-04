package core.framework.impl.web.api.v2;

import core.framework.api.json.Property;
import core.framework.api.validate.NotNull;
import core.framework.api.web.service.Path;
import core.framework.api.web.service.PathParam;
import core.framework.api.web.service.QueryParam;
import core.framework.impl.reflect.Classes;
import core.framework.impl.reflect.GenericTypes;
import core.framework.impl.reflect.Params;
import core.framework.impl.web.service.HTTPMethods;
import core.framework.util.Lists;
import core.framework.util.Maps;

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
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author neo
 */
public class APIDefinitionBuilder {
    private final List<ServiceDefinition> services = Lists.newArrayList();
    private final Map<String, BeanTypeDefinition> beanTypes = Maps.newLinkedHashMap();
    private final Map<String, EnumDefinition> enumTypes = Maps.newLinkedHashMap();

    public void addServiceInterface(Class<?> serviceInterface) {
        ServiceDefinition service = new ServiceDefinition();
        service.name = serviceInterface.getSimpleName();

        Method[] methods = serviceInterface.getMethods();
        Arrays.sort(methods, Comparator.comparing((Method method) -> method.getDeclaredAnnotation(Path.class).value()).thenComparing(method -> HTTPMethods.httpMethod(method).ordinal()));
        for (Method method : methods) {
            ServiceDefinition.Operation operation = new ServiceDefinition.Operation();
            operation.name = method.getName();
            operation.method = HTTPMethods.httpMethod(method);
            operation.path = method.getDeclaredAnnotation(Path.class).value();
            parseParams(operation, method);
            operation.responseType = parseType(method.getGenericReturnType());
            service.operations.add(operation);
        }

        services.add(service);
    }

    public APIDefinitionResponse build() {
        APIDefinitionResponse response = new APIDefinitionResponse();
        response.services = services.stream().map(this::serviceResponse).collect(Collectors.toList());

        response.types = Lists.newArrayList();
        response.types.addAll(beanTypes.entrySet().stream().map(this::beanTypeResponse).collect(Collectors.toList()));
        response.types.addAll(enumTypes.entrySet().stream().map(this::enumTypeResponse).collect(Collectors.toList()));

        return response;
    }

    private APIDefinitionResponse.Service serviceResponse(ServiceDefinition service) {
        APIDefinitionResponse.Service response = new APIDefinitionResponse.Service();
        response.name = service.name;
        response.operations = service.operations.stream().map(this::operationResponse).collect(Collectors.toList());
        return response;
    }

    private APIDefinitionResponse.Operation operationResponse(ServiceDefinition.Operation operation) {
        APIDefinitionResponse.Operation response = new APIDefinitionResponse.Operation();
        response.name = operation.name;
        response.method = operation.method.name();
        response.path = operation.path;
        response.requestType = operation.requestType;
        response.responseType = operation.responseType;
        response.pathParams = operation.pathParams.entrySet().stream().map(this::pathParamResponse).collect(Collectors.toList());
        return response;
    }

    private APIDefinitionResponse.PathParam pathParamResponse(Map.Entry<String, String> entry) {
        APIDefinitionResponse.PathParam response = new APIDefinitionResponse.PathParam();
        response.name = entry.getKey();
        response.type = entry.getValue();
        return response;
    }

    private APIDefinitionResponse.Type beanTypeResponse(Map.Entry<String, BeanTypeDefinition> entry) {
        APIDefinitionResponse.Type type = new APIDefinitionResponse.Type();
        type.name = entry.getKey();
        type.type = "interface";
        StringBuilder builder = new StringBuilder("{ ");
        for (BeanTypeDefinition.Field field : entry.getValue().fields) {
            builder.append(field.name);
            if (!field.notNull) builder.append('?');
            builder.append(": ").append(field.type).append("; ");
        }
        builder.append('}');
        type.definition = builder.toString();
        return type;
    }

    private APIDefinitionResponse.Type enumTypeResponse(Map.Entry<String, EnumDefinition> entry) {
        APIDefinitionResponse.Type type = new APIDefinitionResponse.Type();
        type.name = entry.getKey();
        type.type = "enum";
        StringBuilder builder = new StringBuilder("{ ");
        for (String constant : entry.getValue().constants) {
            builder.append(constant).append(" = \"").append(constant).append("\", ");
        }
        builder.append('}');
        type.definition = builder.toString();
        return type;
    }

    private String parseType(Type type) {
        if (type == void.class) return "void";
        if (GenericTypes.isOptional(type)) return parseType(GenericTypes.optionalValueClass(type)) + " | null";
        if (GenericTypes.isList(type)) {
            String valueType = parseType(GenericTypes.listValueClass(type));
            return valueType + "[]";
        }
        if (GenericTypes.isMap(type)) {
            String valueType = parseType(GenericTypes.mapValueClass(type));
            return "{[key:string]: " + valueType + ";}";
        }
        if (String.class.equals(type)) return "string";
        if (Integer.class.equals(type) || Long.class.equals(type) || Double.class.equals(type) || BigDecimal.class.equals(type)) return "number";
        if (Boolean.class.equals(type)) return "boolean";
        if (LocalDate.class.equals(type) || LocalDateTime.class.equals(type) || ZonedDateTime.class.equals(type) || Instant.class.equals(type)) return "Date";
        if (GenericTypes.rawClass(type).isEnum()) {
            return parseEnum((Class<?>) type);
        }
        return parseBeanType((Class<?>) type);
    }

    private String parseBeanType(Class<?> beanClass) {
        String typeName = Classes.className(beanClass);
        if (!beanTypes.containsKey(typeName)) {
            BeanTypeDefinition definition = new BeanTypeDefinition();
            definition.name = typeName;
            beanTypes.put(typeName, definition);  // put into map to handle circular reference

            for (Field field : Classes.instanceFields(beanClass)) {
                BeanTypeDefinition.Field fieldDefinition = new BeanTypeDefinition.Field();
                fieldDefinition.name = fieldName(field);
                fieldDefinition.type = parseType(field.getGenericType());
                if (field.isAnnotationPresent(NotNull.class)) fieldDefinition.notNull = true;
                definition.fields.add(fieldDefinition);
            }
        }
        return typeName;
    }

    private String parseEnum(Class<?> enumClass) {
        String typeName = Classes.className(enumClass);
        enumTypes.computeIfAbsent(typeName, key -> {
            EnumDefinition definition = new EnumDefinition();
            definition.name = typeName;
            definition.constants = Classes.enumConstantFields(enumClass).stream().map(field -> field.getDeclaredAnnotation(Property.class).name()).collect(Collectors.toList());
            return definition;
        });
        return typeName;
    }

    private void parseParams(ServiceDefinition.Operation operation, Method method) {
        Annotation[][] annotations = method.getParameterAnnotations();
        Type[] paramTypes = method.getGenericParameterTypes();

        for (int i = 0; i < paramTypes.length; i++) {
            String type = parseType(paramTypes[i]);
            PathParam pathParam = Params.annotation(annotations[i], PathParam.class);
            if (pathParam != null) {
                operation.pathParams.put(pathParam.value(), type);
            } else {
                operation.requestType = type;
            }
        }
    }

    private String fieldName(Field field) {
        Property property = field.getDeclaredAnnotation(Property.class);
        if (property != null) return property.name();
        return field.getDeclaredAnnotation(QueryParam.class).name();
    }
}
