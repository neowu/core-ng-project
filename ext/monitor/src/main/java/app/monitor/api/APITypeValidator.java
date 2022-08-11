package app.monitor.api;

import core.framework.internal.web.api.APIType;
import core.framework.log.Severity;
import core.framework.util.Strings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author neo
 */
public class APITypeValidator {
    private static final Set<String> SIMPLE_TYPES = Set.of("String", "Boolean", "Integer", "Long", "Double", "BigDecimal", "LocalDate", "LocalDateTime", "ZonedDateTime", "LocalTime");
    private static final Set<String> COLLECTION_TYPES = Set.of("Map", "List");

    final Map<String, APIType> previousTypes;
    final Map<String, APIType> currentTypes;

    final Set<String> visitedPreviousTypes = new HashSet<>();
    final Set<String> visitedCurrentTypes = new HashSet<>();
    final Map<String, Severity> removedReferenceTypes = new HashMap<>();  // types referred by removed methods and fields

    final APIWarnings warnings;

    public APITypeValidator(Map<String, APIType> previousTypes, Map<String, APIType> currentTypes, APIWarnings warnings) {
        this.previousTypes = previousTypes;
        this.currentTypes = currentTypes;
        this.warnings = warnings;
    }

    void validateTypes() {
        var leftPreviousTypes = new HashMap<>(previousTypes);
        visitedPreviousTypes.forEach(leftPreviousTypes::remove);
        var leftCurrentTypes = new HashMap<>(currentTypes);
        visitedCurrentTypes.forEach(leftCurrentTypes::remove);
        for (var previousType : leftPreviousTypes.values()) {
            var currentType = leftCurrentTypes.remove(previousType.name);
            if (currentType == null) {
                boolean warning = removedReferenceTypes.get(previousType.name) == Severity.WARN;
                warnings.add(warning, "removed type {}", previousType.name);
            } else if (!Strings.equals(previousType.type, currentType.type)) {  // changed bean to enum or vice versa
                warnings.add("changed type {} from {} to {}", previousType.name, previousType.type, currentType.type);
            } else {
                validateType(previousType.name, currentType.name, false);
            }
        }
        for (APIType currentType : leftCurrentTypes.values()) {
            warnings.add(true, "added type {}", currentType.name);
        }
    }

    void validateType(String previousType, String currentType, boolean isRequest) {
        visitedPreviousTypes.add(previousType);
        visitedCurrentTypes.add(currentType);

        APIType previous = previousTypes.get(previousType);
        APIType current = currentTypes.get(currentType);
        if ("enum".equals(previous.type)) {
            validateEnumType(previous, current, isRequest);
        } else {    // bean
            validateBeanType(current, previous, isRequest);
        }
    }

    private void validateEnumType(APIType previous, APIType current, boolean isRequest) {
        var previousEnums = previous.enumConstants.stream().collect(Collectors.toMap(constant -> constant.name, constant -> constant.value));
        var currentEnums = current.enumConstants.stream().collect(Collectors.toMap(constant -> constant.name, constant -> constant.value));
        for (var entry : previousEnums.entrySet()) {
            String previousKey = entry.getKey();
            String previousValue = entry.getValue();
            String currentValue = currentEnums.remove(previousKey);
            if (currentValue == null) {
                warnings.add("removed enum value {}.{}", previous.name, previousKey);
            } else if (!Strings.equals(previousValue, currentValue)) {
                warnings.add("changed enum value of {}.{} from {} to {}", previous.name, previousKey, previousValue, currentValue);
            }
        }
        for (String currentName : currentEnums.keySet()) {
            warnings.add(isRequest, "added enum value {}.{}", previous.name, currentName);
        }
    }

    private APIValidator.CompareTypeResult compareType(String previousType, String currentType) {
        if (SIMPLE_TYPES.contains(previousType) || SIMPLE_TYPES.contains(currentType)
            || COLLECTION_TYPES.contains(previousType) || COLLECTION_TYPES.contains(currentType)) {
            if (!previousType.equals(currentType)) return APIValidator.CompareTypeResult.NOT_MATCH;
            return APIValidator.CompareTypeResult.MATCH;
        } else {
            var previous = previousTypes.get(previousType);
            var current = currentTypes.get(currentType);
            if (previous == null || current == null || !Strings.equals(previous.type, current.type)) return APIValidator.CompareTypeResult.NOT_MATCH;
            return APIValidator.CompareTypeResult.FURTHER_COMPARE;
        }
    }

    private String[] candidateTypes(APIType.Field field) {
        List<String> types = new ArrayList<>();
        types.add(field.type);
        if (field.typeParams != null) types.addAll(field.typeParams);
        return types.toArray(new String[0]);
    }

    private void validateBeanType(APIType current, APIType previous, boolean isRequest) {
        var currentFields = current.fields.stream().collect(Collectors.toMap(field -> field.name, Function.identity()));
        for (APIType.Field previousField : previous.fields) {
            String[] previousTypes = candidateTypes(previousField);

            var currentField = currentFields.remove(previousField.name);
            if (currentField == null) {
                boolean warning = isRequest || !Boolean.TRUE.equals(previousField.constraints.notNull);
                warnings.add(warning, "removed field {}.{}", previous.name, previousField.name);
                Severity severity = warning ? Severity.WARN : Severity.ERROR;
                for (String previousType : previousTypes) {
                    removeReferenceType(previousType, severity);
                }
                continue;
            }

            String[] currentTypes = candidateTypes(currentField);
            if (previousTypes.length != currentTypes.length) {
                warnings.add("changed field type of {}.{} from {} to {}", previous.name, previousField.name, fieldType(previousField), fieldType(currentField));
            } else {
                for (int i = 0; i < previousTypes.length; i++) {
                    String previousCandidateType = previousTypes[i];
                    String currentCandidateType = currentTypes[i];
                    switch (compareType(previousCandidateType, currentCandidateType)) {
                        case NOT_MATCH -> warnings.add("changed field type of {}.{} from {} to {}", previous.name, previousField.name, fieldType(previousField), fieldType(currentField));
                        case FURTHER_COMPARE -> validateType(previousCandidateType, currentCandidateType, isRequest);
                        default -> {
                        }
                    }
                }
            }

            if (!Boolean.TRUE.equals(previousField.constraints.notNull) && Boolean.TRUE.equals(currentField.constraints.notNull)) {
                warnings.add(!isRequest, "added @NotNull to field {}.{}", previous.name, previousField.name);
            } else if (Boolean.TRUE.equals(previousField.constraints.notNull) && !Boolean.TRUE.equals(currentField.constraints.notNull)) {
                warnings.add(isRequest, "removed @NotNull from field {}.{}", previous.name, previousField.name);
            }
        }
        for (var currentField : currentFields.values()) {
            if (isRequest && Boolean.TRUE.equals(currentField.constraints.notNull)) {
                warnings.add("added field @NotNull {}.{}", current.name, currentField.name);
            } else {
                warnings.add(true, "added field {}.{}", current.name, currentField.name);
            }
        }
    }

    void removeReferenceType(String typeName, Severity severity) {
        APIType type = previousTypes.get(typeName);
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

    String fieldType(APIType.Field field) {
        if ("List".equals(field.type)) return "List<" + field.typeParams.get(0) + ">";
        if ("Map".equals(field.type)) {
            if ("List".equals(field.typeParams.get(1))) return "Map<" + field.typeParams.get(0) + ", List<" + field.typeParams.get(2) + ">";
            return "Map<" + field.typeParams.get(0) + ", " + field.typeParams.get(1) + ">";
        }
        return field.type;
    }
}
