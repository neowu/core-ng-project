package core.framework.impl.validate;

/**
 * @author neo
 */
@FunctionalInterface
interface FieldValidator {
    void validate(Object instance, ValidationErrors errors, boolean partial);
}
