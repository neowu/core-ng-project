package core.framework.impl.validate;

/**
 * @author neo
 */
public interface BeanValidator {
    void validate(Object instance, ValidationErrors errors, boolean partial);
}
