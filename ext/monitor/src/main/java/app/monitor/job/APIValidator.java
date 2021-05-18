package app.monitor.job;

import core.framework.internal.web.api.APIDefinitionV2Response;
import core.framework.log.Severity;
import core.framework.util.Strings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author neo
 */
class APIValidator {
    private static final Set<String> SIMPLE_TYPES = Set.of("String", "Boolean", "Integer", "Long", "Double", "BigDecimal", "LocalDate", "LocalDateTime", "ZonedDateTime", "LocalTime");
    private static final Set<String> COLLECTION_TYPES = Set.of("Map", "List");
    final Set<String> warnings = new LinkedHashSet<>();
    final Set<String> errors = new LinkedHashSet<>();

    final Map<String, Operation> previousOperations;
    final Map<String, APIDefinitionV2Response.Type> previousTypes;

    final Map<String, Operation> currentOperations;
    final Map<String, APIDefinitionV2Response.Type> currentTypes;

    final Set<String> visitedPreviousTypes = new HashSet<>();
    final Set<String> visitedCurrentTypes = new HashSet<>();
    final Map<String, Severity> removedReferenceTypes = new HashMap<>();  // types referred by removed methods and fields

    APIValidator(APIDefinitionV2Response previous, APIDefinitionV2Response current) {
        previousOperations = operations(previous);
        previousTypes = previous.types.stream().collect(Collectors.toMap(type -> type.name, Function.identity()));
        currentOperations = operations(current);
        currentTypes = current.types.stream().collect(Collectors.toMap(type -> type.name, Function.identity()));
    }

    String validate() {
        validateOperations();
        validateTypes();
        return result();
    }

    private void validateOperations() {
        for (Map.Entry<String, Operation> entry : previousOperations.entrySet()) {
            Operation previous = entry.getValue();
            Operation current = currentOperations.remove(entry.getKey());
            if (current == null) {
                boolean deprecated = Boolean.TRUE.equals(previous.operation.deprecated);
                addError(deprecated, Strings.format("removed method {}{}", deprecated ? "@Deprecated " : "", previous.methodLiteral()));
                Severity severity = deprecated ? Severity.WARN : Severity.ERROR;
                removeReferenceType(previous.operation.requestType, severity);
                removeReferenceType(previous.operation.responseType, severity);
            } else {
                validateOperation(previous, current);
            }
        }
        if (!currentOperations.isEmpty()) {
            for (Operation operation : currentOperations.values()) {
                warnings.add(Strings.format("added method {}", operation.methodLiteral()));
            }
        }
    }

    private void validateTypes() {
        var leftPreviousTypes = new HashMap<>(previousTypes);
        visitedPreviousTypes.forEach(leftPreviousTypes::remove);
        var leftCurrentTypes = new HashMap<>(currentTypes);
        visitedCurrentTypes.forEach(leftCurrentTypes::remove);
        for (var previousType : leftPreviousTypes.values()) {
            var currentType = leftCurrentTypes.remove(previousType.name);
            if (currentType == null) {
                boolean warning = removedReferenceTypes.get(previousType.name) == Severity.WARN;
                addError(warning, Strings.format("removed type {}", previousType.name));
            } else if (!Strings.equals(previousType.type, currentType.type)) {
                errors.add(Strings.format("changed type {} from {} to {}", previousType.name, previousType.type, currentType.type));
            } else {
                validateType(previousType.name, currentType.name, false);
            }
        }
        for (APIDefinitionV2Response.Type currentType : leftCurrentTypes.values()) {
            warnings.add(Strings.format("added type {}", currentType.name));
        }
    }

    private void validateOperation(Operation previous, Operation current) {
        String previousMethod = previous.methodLiteral();
        String currentMethod = current.methodLiteral();
        if (!Strings.equals(previousMethod, currentMethod)) {
            warnings.add(Strings.format("renamed method {} to {}", previousMethod, currentMethod));
        }

        APIDefinitionV2Response.PathParam[] previousPathParams = previous.operation.pathParams.toArray(new APIDefinitionV2Response.PathParam[0]);
        APIDefinitionV2Response.PathParam[] currentPathParams = current.operation.pathParams.toArray(new APIDefinitionV2Response.PathParam[0]);
        for (int i = 0; i < previousPathParams.length; i++) {   // previous length must equal to current size, as the "method/path" is same
            APIDefinitionV2Response.PathParam previousPathParam = previousPathParams[i];
            APIDefinitionV2Response.PathParam currentPathParam = currentPathParams[i];
            if (!Strings.equals(previousPathParam.type, currentPathParam.type)) {
                errors.add(Strings.format("changed pathParam {} of {} from {} to {}", previousPathParam.name, previousMethod, previousPathParam.type, currentPathParam.type));
            }
        }

        if ((previous.operation.requestType == null || current.operation.requestType == null)
            && !Strings.equals(previous.operation.requestType, current.operation.requestType)) {
            errors.add(Strings.format("changed request type of {} from {} to {}", previousMethod, previous.operation.requestType, current.operation.requestType));
        } else if (previous.operation.requestType != null && current.operation.requestType != null) {
            if (!Strings.equals(previous.operation.requestType, current.operation.requestType)) {
                warnings.add(Strings.format("renamed request type of {} from {} to {}", previousMethod, previous.operation.requestType, current.operation.requestType));
            }
            validateType(previous.operation.requestType, current.operation.requestType, true);
        }

        if (Boolean.compare(previous.optional(), current.optional()) != 0) {
            errors.add(Strings.format("changed response type of {} from {} to {}", previousMethod, previous.responseTypeLiteral(), current.responseTypeLiteral()));
        } else if ("void".equals(previous.operation.responseType) || "void".equals(current.operation.responseType)) {
            if (!Strings.equals(previous.operation.responseType, current.operation.responseType)) {
                errors.add(Strings.format("changed response type of {} from {} to {}", previousMethod, previous.responseTypeLiteral(), current.responseTypeLiteral()));
            }
        } else {
            validateType(previous.operation.responseType, current.operation.responseType, false);
        }

        if (Boolean.compare(previous.deprecated(), current.deprecated()) != 0) {
            if (previous.deprecated())
                warnings.add(Strings.format("removed @Deprecated from method {}", previousMethod));
            else
                warnings.add(Strings.format("added @Deprecated to method {}", previousMethod));
        }
    }

    private void validateType(String previousType, String currentType, boolean isRequest) {
        visitedPreviousTypes.add(previousType);
        visitedCurrentTypes.add(currentType);

        APIDefinitionV2Response.Type previous = previousTypes.get(previousType);
        APIDefinitionV2Response.Type current = currentTypes.get(currentType);
        if ("enum".equals(previous.type)) {
            validateEnumType(previous, current, isRequest);
        } else {    // bean
            validateBeanType(current, previous, isRequest);
        }
    }

    private void validateBeanType(APIDefinitionV2Response.Type current, APIDefinitionV2Response.Type previous, boolean isRequest) {
        var currentFields = current.fields.stream().collect(Collectors.toMap(field -> field.name, Function.identity()));
        for (APIDefinitionV2Response.Field previousField : previous.fields) {
            String[] previousTypes = candidateTypes(previousField);

            var currentField = currentFields.remove(previousField.name);
            if (currentField == null) {
                boolean warning = isRequest || !Boolean.TRUE.equals(previousField.constraints.notNull);
                addError(warning, Strings.format("removed field {}.{}", previous.name, previousField.name));
                Severity severity = warning ? Severity.WARN : Severity.ERROR;
                for (String previousType : previousTypes) {
                    removeReferenceType(previousType, severity);
                }
                continue;
            }

            String[] currentTypes = candidateTypes(currentField);
            if (previousTypes.length != currentTypes.length) {
                errors.add(Strings.format("changed field type of {}.{} from {} to {}", previous.name, previousField.name, fieldType(previousField), fieldType(currentField)));
            } else {
                for (int i = 0; i < previousTypes.length; i++) {
                    String previousCandidateType = previousTypes[i];
                    String currentCandidateType = currentTypes[i];
                    switch (compareType(previousCandidateType, currentCandidateType)) {
                        case NOT_MATCH -> errors.add(Strings.format("changed field type of {}.{} from {} to {}", previous.name, previousField.name, fieldType(previousField), fieldType(currentField)));
                        case FURTHER_COMPARE -> validateType(previousCandidateType, currentCandidateType, isRequest);
                        default -> {
                        }
                    }
                }
            }

            if (!Boolean.TRUE.equals(previousField.constraints.notNull) && Boolean.TRUE.equals(currentField.constraints.notNull)) {
                addError(!isRequest, Strings.format("added @NotNull to field {}.{}", previous.name, previousField.name));
            } else if (Boolean.TRUE.equals(previousField.constraints.notNull) && !Boolean.TRUE.equals(currentField.constraints.notNull)) {
                addError(isRequest, Strings.format("removed @NotNull from field {}.{}", previous.name, previousField.name));
            }
        }
        for (var currentField : currentFields.values()) {
            if (isRequest && Boolean.TRUE.equals(currentField.constraints.notNull)) {
                errors.add(Strings.format("added field @NotNull {}.{}", current.name, currentField.name));
            } else {
                warnings.add(Strings.format("added field {}.{}", current.name, currentField.name));
            }
        }
    }

    private void validateEnumType(APIDefinitionV2Response.Type previous, APIDefinitionV2Response.Type current, boolean isRequest) {
        var previousEnums = previous.enumConstants.stream().collect(Collectors.toMap(constant -> constant.name, constant -> constant.value));
        var currentEnums = current.enumConstants.stream().collect(Collectors.toMap(constant -> constant.name, constant -> constant.value));
        for (var entry : previousEnums.entrySet()) {
            String previousKey = entry.getKey();
            String previousValue = entry.getValue();
            String currentValue = currentEnums.remove(previousKey);
            if (currentValue == null) {
                errors.add(Strings.format("removed enum value {}.{}", previous.name, previousKey));
            } else if (!Strings.equals(previousValue, currentValue)) {
                errors.add(Strings.format("changed enum value of {}.{} from {} to {}", previous.name, previousKey, previousValue, currentValue));
            }
        }
        for (String currentName : currentEnums.keySet()) {
            addError(isRequest, Strings.format("added enum value {}.{}", previous.name, currentName));
        }
    }

    private CompareTypeResult compareType(String previousType, String currentType) {
        if (SIMPLE_TYPES.contains(previousType) || SIMPLE_TYPES.contains(currentType)
            || COLLECTION_TYPES.contains(previousType) || COLLECTION_TYPES.contains(currentType)) {
            if (!previousType.equals(currentType)) return CompareTypeResult.NOT_MATCH;
            return CompareTypeResult.MATCH;
        } else {
            var previous = previousTypes.get(previousType);
            var current = currentTypes.get(currentType);
            if (previous == null || current == null || !Strings.equals(previous.type, current.type)) return CompareTypeResult.NOT_MATCH;
            return CompareTypeResult.FURTHER_COMPARE;
        }
    }

    private String[] candidateTypes(APIDefinitionV2Response.Field field) {
        List<String> types = new ArrayList<>();
        types.add(field.type);
        if (field.typeParams != null) types.addAll(field.typeParams);
        return types.toArray(String[]::new);
    }

    private void removeReferenceType(String typeName, Severity severity) {
        APIDefinitionV2Response.Type type = previousTypes.get(typeName);
        if (type == null) return;   // not bean type, e.g. simple type, collection type, void, null
        Severity value = severity;
        if (value == Severity.WARN) value = removedReferenceTypes.getOrDefault(typeName, Severity.WARN);
        removedReferenceTypes.put(typeName, value);

        if ("bean".equals(type.type)) {
            for (var field : type.fields) {
                final String[] candidateTypes = candidateTypes(field);
                for (String candidateType : candidateTypes) {
                    removeReferenceType(candidateType, severity);
                }
            }
        }
    }

    private Map<String, Operation> operations(APIDefinitionV2Response response) {
        Map<String, Operation> operations = new LinkedHashMap<>();
        for (APIDefinitionV2Response.Service service : response.services) {
            for (APIDefinitionV2Response.Operation operation : service.operations) {
                operations.put(operation.method + "/" + operation.path, new Operation(service.name, operation));
            }
        }
        return operations;
    }

    void addError(boolean warning, String error) {
        if (warning) {
            warnings.add(error);
        } else {
            errors.add(error);
        }
    }

    String fieldType(APIDefinitionV2Response.Field field) {
        if ("List".equals(field.type)) return "List<" + field.typeParams.get(0) + ">";
        if ("Map".equals(field.type)) {
            if ("List".equals(field.typeParams.get(1))) return "Map<" + field.typeParams.get(0) + ", List<" + field.typeParams.get(2) + ">";
            return "Map<" + field.typeParams.get(0) + ", " + field.typeParams.get(1) + ">";
        }
        return field.type;
    }

    String result() {
        if (!errors.isEmpty()) return "ERROR";
        if (!warnings.isEmpty()) return "WARN";
        return null;
    }

    public String errorMessage() {
        StringBuilder builder = new StringBuilder();
        if (!errors.isEmpty()) {
            builder.append("*incompatible changes*\n");
            errors.forEach(error -> builder.append("* ").append(error).append('\n'));
        }
        if (!warnings.isEmpty()) {
            builder.append("*compatible changes*\n");
            warnings.forEach(warning -> builder.append("* ").append(warning).append('\n'));
        }
        return builder.toString();
    }

    enum CompareTypeResult {
        MATCH,  // simple types and match
        NOT_MATCH,  // one is simple or oen is bean, another is enum, and not match, stop comparing
        FURTHER_COMPARE // both are bean or enum, require further compare
    }

    static class Operation {
        String service;
        APIDefinitionV2Response.Operation operation;

        Operation(String service, APIDefinitionV2Response.Operation operation) {
            this.service = service;
            this.operation = operation;
        }

        String methodLiteral() {
            return Strings.format("{}.{}", service, operation.name);
        }

        String responseTypeLiteral() {
            if (operation.optional) {
                return Strings.format("Optional<{}>", operation.responseType);
            }
            return operation.responseType;
        }

        boolean optional() {
            return Boolean.TRUE.equals(operation.optional);
        }

        boolean deprecated() {
            return Boolean.TRUE.equals(operation.deprecated);
        }
    }
}
