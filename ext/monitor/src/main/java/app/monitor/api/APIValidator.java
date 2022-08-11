package app.monitor.api;

import core.framework.internal.web.api.APIDefinitionResponse;
import core.framework.internal.web.api.APIType;
import core.framework.log.Severity;
import core.framework.util.Strings;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author neo
 */
public class APIValidator {
    private final Map<String, Operation> previousOperations;
    private final Map<String, Operation> currentOperations;
    private final APITypeValidator typeValidator;
    private final APIWarnings warnings = new APIWarnings();

    public APIValidator(APIDefinitionResponse previous, APIDefinitionResponse current) {
        previousOperations = operations(previous);
        currentOperations = operations(current);
        Map<String, APIType> previousTypes = previous.types.stream().collect(Collectors.toMap(type -> type.name, Function.identity()));
        Map<String, APIType> currentTypes = current.types.stream().collect(Collectors.toMap(type -> type.name, Function.identity()));
        typeValidator = new APITypeValidator(previousTypes, currentTypes, warnings);
    }

    public APIWarnings validate() {
        validateOperations();
        typeValidator.validateTypes();
        warnings.removeDuplicateWarnings();
        return warnings;
    }

    private void validateOperations() {
        for (Map.Entry<String, Operation> entry : previousOperations.entrySet()) {
            Operation previous = entry.getValue();
            Operation current = currentOperations.remove(entry.getKey());
            if (current == null) {
                boolean deprecated = Boolean.TRUE.equals(previous.operation.deprecated);
                warnings.add(deprecated, "removed method {}", previous.signature());
                Severity severity = deprecated ? Severity.WARN : Severity.ERROR;
                typeValidator.removeReferenceType(previous.operation.requestType, severity);
                typeValidator.removeReferenceType(previous.operation.responseType, severity);
            } else {
                validateOperation(previous, current);
            }
        }
        if (!currentOperations.isEmpty()) {
            for (Operation operation : currentOperations.values()) {
                warnings.add(true, "added method {}", operation.signature());
            }
        }
    }

    private void validateOperation(Operation previous, Operation current) {
        String previousMethod = previous.methodLiteral();
        String currentMethod = current.methodLiteral();
        if (!Strings.equals(previousMethod, currentMethod)) {
            warnings.add(true, "renamed method {} to {}", previousMethod, currentMethod);
        }

        APIDefinitionResponse.PathParam[] previousPathParams = previous.operation.pathParams.toArray(new APIDefinitionResponse.PathParam[0]);
        APIDefinitionResponse.PathParam[] currentPathParams = current.operation.pathParams.toArray(new APIDefinitionResponse.PathParam[0]);
        for (int i = 0; i < previousPathParams.length; i++) {   // previous length must equal to current size, as the "method/path" is same
            APIDefinitionResponse.PathParam previousPathParam = previousPathParams[i];
            APIDefinitionResponse.PathParam currentPathParam = currentPathParams[i];
            if (!Strings.equals(previousPathParam.type, currentPathParam.type)) {
                warnings.add("changed pathParam {} of {} from {} to {}", previousPathParam.name, previousMethod, previousPathParam.type, currentPathParam.type);
            }
        }

        if ((previous.operation.requestType == null || current.operation.requestType == null)
            && !Strings.equals(previous.operation.requestType, current.operation.requestType)) {
            warnings.add("changed request type of {} from {} to {}", previousMethod, previous.operation.requestType, current.operation.requestType);
        } else if (previous.operation.requestType != null && current.operation.requestType != null) {
            if (!Strings.equals(previous.operation.requestType, current.operation.requestType)) {
                warnings.add(true, "renamed request type of {} from {} to {}", previousMethod, previous.operation.requestType, current.operation.requestType);
            }
            typeValidator.validateType(previous.operation.requestType, current.operation.requestType, true);
        }

        if (Boolean.compare(previous.optional(), current.optional()) != 0) {
            warnings.add("changed response type of {} from {} to {}", previousMethod, previous.responseTypeLiteral(), current.responseTypeLiteral());
        } else if ("void".equals(previous.operation.responseType) || "void".equals(current.operation.responseType)) {
            if (!Strings.equals(previous.operation.responseType, current.operation.responseType)) {
                warnings.add("changed response type of {} from {} to {}", previousMethod, previous.responseTypeLiteral(), current.responseTypeLiteral());
            }
        } else {    // both are not void
            typeValidator.validateType(previous.operation.responseType, current.operation.responseType, false);
        }

        if (Boolean.compare(previous.deprecated(), current.deprecated()) != 0) {
            if (previous.deprecated())
                warnings.add(true, "removed @Deprecated from method {}", previousMethod);
            else
                warnings.add(true, "added @Deprecated to method {}", previousMethod);
        }
    }

    private Map<String, Operation> operations(APIDefinitionResponse response) {
        Map<String, Operation> operations = new LinkedHashMap<>();
        for (APIDefinitionResponse.Service service : response.services) {
            for (APIDefinitionResponse.Operation operation : service.operations) {
                operations.put(operation.method + "/" + operation.path, new Operation(service.name, operation));
            }
        }
        return operations;
    }

    enum CompareTypeResult {
        MATCH,  // simple types and match
        NOT_MATCH,  // one is simple or oen is bean, another is enum, and not match, stop comparing
        FURTHER_COMPARE // both are bean or enum, require further compare
    }

    static class Operation {
        String service;
        APIDefinitionResponse.Operation operation;

        Operation(String service, APIDefinitionResponse.Operation operation) {
            this.service = service;
            this.operation = operation;
        }

        String signature() {
            var builder = new StringBuilder(64);
            if (deprecated()) builder.append("@Deprecated ");
            builder.append('@').append(operation.method)
                .append(" @Path(\"").append(operation.path).append("\") ")
                .append(service).append('.').append(operation.name);
            return builder.toString();
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
