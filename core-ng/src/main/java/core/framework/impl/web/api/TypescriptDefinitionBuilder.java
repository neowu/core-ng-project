package core.framework.impl.web.api;

import core.framework.api.json.Property;
import core.framework.api.validate.NotNull;
import core.framework.api.web.service.Path;
import core.framework.api.web.service.PathParam;
import core.framework.api.web.service.QueryParam;
import core.framework.impl.asm.CodeBuilder;
import core.framework.impl.reflect.Classes;
import core.framework.impl.reflect.GenericTypes;
import core.framework.impl.reflect.Params;
import core.framework.impl.web.service.HTTPMethods;
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
public class TypescriptDefinitionBuilder {
    private final Map<String, Namespace> namespaces = Maps.newLinkedHashMap();

    public void addServiceInterface(Class<?> serviceInterface) {
        ServiceDefinition serviceDefinition = new ServiceDefinition();
        serviceDefinition.name = serviceInterface.getSimpleName();

        Method[] methods = serviceInterface.getMethods();
        Arrays.sort(methods, Comparator.comparing((Method method) -> method.getDeclaredAnnotation(Path.class).value()).thenComparing(method -> HTTPMethods.httpMethod(method).ordinal()));
        for (Method method : methods) {
            ServiceDefinition.ServiceMethodDefinition methodDefinition = new ServiceDefinition.ServiceMethodDefinition();
            methodDefinition.name = method.getName();
            methodDefinition.method = HTTPMethods.httpMethod(method);
            methodDefinition.path = method.getDeclaredAnnotation(Path.class).value();
            parseParams(methodDefinition, method);
            methodDefinition.responseType = parseType(method.getGenericReturnType());
            serviceDefinition.methods.add(methodDefinition);
        }

        namespace(serviceInterface.getPackage()).serviceDefinitions.add(serviceDefinition);
    }

    private Namespace namespace(Package classPackage) {
        return namespaces.computeIfAbsent(classPackage.getName(), key -> new Namespace());
    }

    public String build() {
        CodeBuilder builder = new CodeBuilder();
        namespaces.forEach((name, namespace) -> {
            builder.append("export namespace ").append(name).append(" {\n");
            buildTypes(builder, namespace.typeDefinitions);
            buildEnums(builder, namespace.enumDefinitions);
            buildServices(builder, namespace.serviceDefinitions);
            builder.append("}\n");
        });
        return builder.build();
    }

    private void buildServices(CodeBuilder builder, List<ServiceDefinition> serviceDefinitions) {
        for (ServiceDefinition definition : serviceDefinitions) {
            builder.indent(1).append("export const ").append(definition.name).append("Metadata = {\n");
            for (ServiceDefinition.ServiceMethodDefinition method : definition.methods) {
                builder.indent(2).append(method.name).append(": { method: \"").append(method.method.name()).append("\", path: \"").append(method.path).append("\" },\n");
            }
            builder.indent(1).append("};\n");

            builder.indent(1).append("export interface ").append(definition.name).append(" {\n");
            for (ServiceDefinition.ServiceMethodDefinition method : definition.methods) {
                builder.indent(2).append(method.name).append("(");
                builder.append(String.join(", ", method.params.entrySet().stream().map(entry -> entry.getKey() + ": " + entry.getValue()).collect(Collectors.toList())));
                builder.append("): Promise<").append(method.responseType).append(">;\n");
            }
            builder.indent(1).append("}\n");
        }
    }

    private void buildEnums(CodeBuilder builder, Map<String, EnumDefinition> enumDefinitions) {
        enumDefinitions.forEach((name, definition) -> {
            builder.indent(1).append("export enum ").append(name).append(" {\n");
            builder.indent(2).append(String.join(", ", definition.constants)).append('\n');
            builder.indent(1).append("}\n");
        });
    }

    private void buildTypes(CodeBuilder builder, Map<String, TypeDefinition> typeDefinitions) {
        typeDefinitions.forEach((name, definition) -> {
            builder.indent(1).append("export interface ").append(name).append(" {\n");
            for (TypeDefinition.FieldDefinition field : definition.fields) {
                builder.indent(2).append(field.name);
                if (!field.notNull) builder.append('?');
                builder.append(": ").append(field.type).append(";\n");
            }
            builder.indent(1).append("}\n");
        });
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
            return "{ [key:string]: " + valueType + "; }";
        }
        if (String.class.equals(type)) return "string";
        if (Integer.class.equals(type) || Long.class.equals(type) || Double.class.equals(type) || BigDecimal.class.equals(type)) return "number";
        if (Boolean.class.equals(type)) return "boolean";
        if (LocalDate.class.equals(type) || LocalDateTime.class.equals(type) || ZonedDateTime.class.equals(type) || Instant.class.equals(type)) return "Date";
        if (GenericTypes.rawClass(type).isEnum()) {
            @SuppressWarnings("unchecked")
            Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>) type;
            return parseEnum(enumClass);
        }
        return parseBeanType((Class<?>) type);
    }

    private String parseBeanType(Class<?> beanClass) {
        Map<String, TypeDefinition> typeDefinitions = namespace(beanClass.getPackage()).typeDefinitions;
        String typeName = typeName(beanClass);
        if (!typeDefinitions.containsKey(typeName)) {
            TypeDefinition definition = new TypeDefinition();
            definition.name = typeName;
            typeDefinitions.put(typeName, definition);  // put into map to handle circular reference

            for (Field field : Classes.instanceFields(beanClass)) {
                TypeDefinition.FieldDefinition fieldDefinition = new TypeDefinition.FieldDefinition();
                fieldDefinition.name = fieldName(field);
                fieldDefinition.type = parseType(field.getGenericType());
                if (field.isAnnotationPresent(NotNull.class)) fieldDefinition.notNull = true;
                definition.fields.add(fieldDefinition);
            }
        }
        return beanClass.getName();
    }

    private String parseEnum(Class<? extends Enum<?>> enumClass) {
        String typeName = typeName(enumClass);
        namespace(enumClass.getPackage()).enumDefinitions.computeIfAbsent(typeName, key -> {
            EnumDefinition definition = new EnumDefinition();
            definition.name = typeName;
            @SuppressWarnings("rawtypes")
            List<String> constants = Arrays.stream(enumClass.getEnumConstants()).map(Enum::name).collect(Collectors.toList());
            definition.constants.addAll(constants);
            return definition;
        });
        return enumClass.getName();
    }

    private void parseParams(ServiceDefinition.ServiceMethodDefinition methodDefinition, Method method) {
        Annotation[][] annotations = method.getParameterAnnotations();
        Type[] paramTypes = method.getGenericParameterTypes();

        for (int i = 0; i < paramTypes.length; i++) {
            String type = parseType(paramTypes[i]);
            PathParam pathParam = Params.annotation(annotations[i], PathParam.class);
            if (pathParam != null) {
                methodDefinition.params.put(pathParam.value(), type);
            } else {
                methodDefinition.params.put("request", type);
            }
        }
    }

    private String typeName(Class<?> beanClass) {
        String name = beanClass.getName();
        int index = name.lastIndexOf('.');
        if (index >= 0) return name.substring(index + 1);
        return name;
    }

    private String fieldName(Field field) {
        Property property = field.getDeclaredAnnotation(Property.class);
        if (property != null) return property.name();
        return field.getDeclaredAnnotation(QueryParam.class).name();
    }
}
