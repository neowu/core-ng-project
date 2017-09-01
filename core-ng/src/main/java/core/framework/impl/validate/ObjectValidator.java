package core.framework.impl.validate;

/**
 * @author neo
 */
public interface ObjectValidator {
    void validate(Object instance, ValidationErrors errors, boolean partial);
}
