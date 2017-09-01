package core.framework.impl.validate.v2;

/**
 * @author neo
 */
public interface ObjectValidator {
    void validate(Object instance, ValidationErrors errors, boolean partial);
}
