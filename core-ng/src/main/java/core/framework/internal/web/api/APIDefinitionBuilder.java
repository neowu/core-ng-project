package core.framework.internal.web.api;

import core.framework.api.json.Property;
import core.framework.api.validate.Max;
import core.framework.api.validate.Min;
import core.framework.api.validate.NotBlank;
import core.framework.api.validate.NotNull;
import core.framework.api.validate.Pattern;
import core.framework.api.validate.Size;
import core.framework.api.web.service.Path;
import core.framework.api.web.service.PathParam;
import core.framework.api.web.service.QueryParam;
import core.framework.internal.log.LogManager;
import core.framework.internal.reflect.Classes;
import core.framework.internal.reflect.GenericTypes;
import core.framework.internal.reflect.Params;
import core.framework.internal.web.service.HTTPMethods;
import core.framework.util.Maps;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author neo
 */
public class APIDefinitionBuilder {
    private final Set<Class<?>> serviceInterfaces;
    private final Set<Class<?>> beanClasses;
    private final Map<String, APIDefinitionResponse.Type> types = Maps.newLinkedHashMap();
    private final Set<Class<?>> valueClasses = Set.of(String.class, Boolean.class,
        Integer.class, Long.class, Double.class, BigDecimal.class,
        LocalDate.class, LocalDateTime.class, ZonedDateTime.class, LocalTime.class);

    public APIDefinitionBuilder(Set<Class<?>> serviceInterfaces, Set<Class<?>> beanClasses) {
        this.serviceInterfaces = serviceInterfaces;
        this.beanClasses = beanClasses;
    }

    public APIDefinitionResponse build() {
        var response = new APIDefinitionResponse();
        response.app = LogManager.APP_NAME;
        response.services = services();
        for (Class<?> beanClass : beanClasses) {
            if (beanClass.isEnum()) {
                parseEnumType(beanClass);
            } else {
                parseBeanType(beanClass);
            }
        }
        response.types = new ArrayList<>(types.values());
        return response;
    }

    private List<APIDefinitionResponse.Service> services() {
        var services = new ArrayList<APIDefinitionResponse.Service>(serviceInterfaces.size());
        for (Class<?> serviceInterface : serviceInterfaces) {
            services.add(service(serviceInterface));
        }
        return services;
    }

    private APIDefinitionResponse.Service service(Class<?> serviceInterface) {
        var service = new APIDefinitionResponse.Service();
        service.name = serviceInterface.getSimpleName();
        Method[] methods = serviceInterface.getMethods();
        Arrays.sort(methods, Comparator.comparing((Method method) -> method.getDeclaredAnnotation(Path.class).value()).thenComparing(method -> HTTPMethods.httpMethod(method).ordinal()));
        service.operations = new ArrayList<>(methods.length);
        for (Method method : methods) {
            var operation = new APIDefinitionResponse.Operation();
            operation.name = method.getName();
            operation.method = String.valueOf(HTTPMethods.httpMethod(method));
            operation.path = method.getDeclaredAnnotation(Path.class).value();
            parseParams(operation, method);
            operation.optional = GenericTypes.isOptional(method.getGenericReturnType());
            operation.responseType = parseResponseType(method.getGenericReturnType(), operation.optional);
            operation.deprecated = method.isAnnotationPresent(Deprecated.class);
            service.operations.add(operation);
        }
        return service;
    }

    private String parseResponseType(Type returnType, boolean optional) {
        // response can only be Optional<T> or Class
        if (optional) {
            return parseType(GenericTypes.optionalValueClass(returnType)).type;
        }
        return parseType(returnType).type;
    }

    private void parseParams(APIDefinitionResponse.Operation operation, Method method) {
        Annotation[][] annotations = method.getParameterAnnotations();
        Type[] paramTypes = method.getGenericParameterTypes();
        for (int i = 0; i < paramTypes.length; i++) {
            var type = parseType(paramTypes[i]);
            PathParam pathParam = Params.annotation(annotations[i], PathParam.class);
            if (pathParam != null) {
                var param = new APIDefinitionResponse.PathParam();
                param.name = pathParam.value();
                param.type = type.type; // path param type must be simple type
                operation.pathParams.add(param);
            } else {
                operation.requestType = type.type;
            }
        }
    }

    public TypeDefinition parseType(Type type) {
        if (type == void.class) return new TypeDefinition("void");
        if (GenericTypes.isList(type)) {
            var valueType = parseType(GenericTypes.listValueClass(type));
            return new TypeDefinition("List").addParam(valueType.type);
        }
        if (GenericTypes.isMap(type)) {
            Class<?> keyClass = GenericTypes.mapKeyClass(type);
            var definition = new TypeDefinition("Map");
            if (String.class.equals(keyClass)) {
                definition.addParam("String");
            } else {        // Map key can only be String or enum
                definition.addParam(parseEnumType(keyClass));
            }
            var valueType = parseType(GenericTypes.mapValueType(type));
            definition.addParam(valueType.type);
            if (valueType.params != null) definition.addParam(valueType.params.get(0));    // Map<T, List<V>> is most complex type
            return definition;
        }
        Class<?> rawClass = GenericTypes.rawClass(type);  // it must be class type at this point
        if (valueClasses.contains(rawClass)) return new TypeDefinition(rawClass.getSimpleName());
        if (Instant.class.equals(type)) return new TypeDefinition("ZonedDateTime");
        if (rawClass.isEnum()) {
            return new TypeDefinition(parseEnumType((Class<?>) type));
        }
        return new TypeDefinition(parseBeanType(rawClass));
    }

    private String parseBeanType(Class<?> beanClass) {
        String className = Classes.className(beanClass);
        if (!types.containsKey(className)) {
            List<Field> fields = Classes.instanceFields(beanClass);
            var definition = new APIDefinitionResponse.Type();
            definition.type = "bean";
            definition.name = className;
            definition.fields = new ArrayList<>(fields.size());
            types.put(className, definition);  // put into map before parsing fields to handle circular reference
            for (Field field : fields) {
                var fieldDefinition = new APIDefinitionResponse.Field();
                fieldDefinition.name = fieldName(field);
                var type = parseType(field.getGenericType());
                fieldDefinition.type = type.type;
                fieldDefinition.typeParams = type.params;
                parseConstraints(field, fieldDefinition.constraints);
                definition.fields.add(fieldDefinition);
            }
        }
        return className;
    }

    private String parseEnumType(Class<?> enumClass) {
        String className = Classes.className(enumClass);
        types.computeIfAbsent(className, key -> {
            List<Field> fields = Classes.enumConstantFields(enumClass);
            var definition = new APIDefinitionResponse.Type();
            definition.type = "enum";
            definition.name = className;
            definition.enumConstants = new ArrayList<>(fields.size());
            for (Field field : fields) {
                var constant = new APIDefinitionResponse.EnumConstant();
                constant.name = field.getName();
                constant.value = field.getDeclaredAnnotation(Property.class).name();
                definition.enumConstants.add(constant);
            }
            return definition;
        });
        return className;
    }

    private void parseConstraints(Field field, APIDefinitionResponse.Constraints constraints) {
        constraints.notNull = field.isAnnotationPresent(NotNull.class);
        constraints.notBlank = field.isAnnotationPresent(NotBlank.class);
        Min min = field.getDeclaredAnnotation(Min.class);
        if (min != null) constraints.min = min.value();
        Max max = field.getDeclaredAnnotation(Max.class);
        if (max != null) constraints.max = max.value();
        Size size = field.getDeclaredAnnotation(Size.class);
        if (size != null) {
            constraints.size = new APIDefinitionResponse.Size();
            constraints.size.min = size.min();
            constraints.size.max = size.max();
        }
        Pattern pattern = field.getDeclaredAnnotation(Pattern.class);
        if (pattern != null) constraints.pattern = pattern.value();
    }

    private String fieldName(Field field) {
        Property property = field.getDeclaredAnnotation(Property.class);
        if (property != null) return property.name();
        return field.getDeclaredAnnotation(QueryParam.class).name();
    }

    static class TypeDefinition {
        final String type;
        List<String> params;

        TypeDefinition(String type) {
            this.type = type;
        }

        TypeDefinition addParam(String param) {
            if (params == null) params = new ArrayList<>();
            params.add(param);
            return this;
        }
    }
}
