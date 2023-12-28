package core.framework.internal.web.bean;

import core.framework.api.json.Property;
import core.framework.api.web.service.QueryParam;
import core.framework.internal.bean.BeanClassNameValidator;
import core.framework.internal.json.JSONClassValidator;
import core.framework.internal.reflect.Fields;
import core.framework.internal.validate.ClassValidatorSupport;
import core.framework.internal.validate.ClassVisitor;
import core.framework.util.Sets;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Set;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
final class QueryParamClassValidator implements ClassVisitor {
    private final ClassValidatorSupport support = new ClassValidatorSupport();
    private final BeanClassNameValidator beanClassNameValidator;
    private final Class<?> beanClass;
    private final Set<Class<?>> allowedValueClasses = Set.of(String.class, Boolean.class,
        Integer.class, Long.class, Double.class, BigDecimal.class,
        LocalDate.class, LocalDateTime.class, ZonedDateTime.class, LocalTime.class);
    private final Set<String> visitedParams = Sets.newHashSet();

    QueryParamClassValidator(Class<?> beanClass, BeanClassNameValidator beanClassNameValidator) {
        this.beanClass = beanClass;
        this.beanClassNameValidator = beanClassNameValidator;
    }

    public void validate() {
        support.validateClass(beanClass);
        beanClassNameValidator.validate(beanClass);

        for (Field field : support.declaredFields(beanClass)) {
            validateAnnotations(field);
            validateValueField(field);
        }
    }

    private void validateAnnotations(Field field) {
        QueryParam queryParam = field.getDeclaredAnnotation(QueryParam.class);
        if (queryParam == null)
            throw new Error("field must have @QueryParam, field=" + Fields.path(field));

        Property property = field.getDeclaredAnnotation(Property.class);
        if (property != null)
            throw new Error("field must not have @Property, field=" + Fields.path(field));

        String name = queryParam.name();
        boolean added = visitedParams.add(name);
        if (!added)
            throw new Error(format("found duplicate query param, field={}, name={}", Fields.path(field), name));
    }

    private void validateValueField(Field field) {
        Class<?> fieldClass = field.getType();

        if (fieldClass.isEnum()) {  // enum is allowed value type
            beanClassNameValidator.validate(fieldClass);
            JSONClassValidator.validateEnum(fieldClass);
            return;
        }

        if (allowedValueClasses.contains(fieldClass)) return;

        if (fieldClass.getPackageName().startsWith("java"))
            throw new Error(format("field class is not supported, class={}, field={}", fieldClass.getCanonicalName(), Fields.path(field)));

        throw new Error(format("child object is not allowed, class={}, field={}", fieldClass.getCanonicalName(), Fields.path(field)));
    }
}
