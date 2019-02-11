package core.framework.internal.validate;

/**
 * @author neo
 */
public interface BeanValidator {
    void validate(Object instance, ValidationErrors errors, boolean partial);
}
