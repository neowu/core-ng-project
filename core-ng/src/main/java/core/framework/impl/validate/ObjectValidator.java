package core.framework.impl.validate;

import core.framework.api.util.Lists;
import core.framework.api.util.Maps;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public class ObjectValidator implements FieldValidator {
    private final Map<Field, List<FieldValidator>> validators = Maps.newHashMap();

    void add(Field field, FieldValidator validator) {
        validators.computeIfAbsent(field, key -> Lists.newArrayList())
            .add(validator);
    }

    boolean empty() {
        return validators.isEmpty();
    }

    @Override
    public void validate(Object instance, ValidationErrors errors) {
        if (instance != null) {
            validators.forEach((field, validators) -> {
                try {
                    Object fieldValue = field.get(instance);
                    validators.forEach(validator -> validator.validate(fieldValue, errors));
                } catch (IllegalAccessException e) {
                    throw new Error(e);
                }
            });
        }
    }
}
