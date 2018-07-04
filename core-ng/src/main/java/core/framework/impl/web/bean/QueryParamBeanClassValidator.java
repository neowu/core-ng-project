package core.framework.impl.web.bean;

import core.framework.api.json.Property;
import core.framework.api.web.service.QueryParam;
import core.framework.impl.reflect.Fields;
import core.framework.impl.validate.type.DataTypeValidator;
import core.framework.impl.validate.type.JSONTypeValidator;
import core.framework.impl.validate.type.TypeVisitor;
import core.framework.util.Exceptions;
import core.framework.util.Sets;

import java.lang.reflect.Field;
import java.util.Set;

/**
 * @author neo
 */
final class QueryParamBeanClassValidator implements TypeVisitor {
    private final DataTypeValidator validator;
    private final Set<String> visitedQueryParams = Sets.newHashSet();
    private final BeanClassNameValidator classNameValidator;

    QueryParamBeanClassValidator(Class<?> beanClass, BeanClassNameValidator classNameValidator) {
        this.classNameValidator = classNameValidator;
        validator = new DataTypeValidator(beanClass);
        validator.visitor = this;
    }

    public void validate() {
        validator.validate();
    }

    @Override
    public void visitClass(Class<?> objectClass, String path) {
        classNameValidator.validateBeanClass(objectClass);
    }

    @Override
    public void visitField(Field field, String parentPath) {
        QueryParam queryParam = field.getDeclaredAnnotation(QueryParam.class);
        if (queryParam == null)
            throw Exceptions.error("field must have @QueryParam, field={}", Fields.path(field));

        Property property = field.getDeclaredAnnotation(Property.class);
        if (property != null)
            throw Exceptions.error("field must not have @Property, field={}", Fields.path(field));

        String name = queryParam.name();

        boolean added = visitedQueryParams.add(queryParam.name());
        if (!added)
            throw Exceptions.error("found duplicate query param, field={}, name={}", Fields.path(field), name);
    }

    @Override
    public void visitEnum(Class<?> enumClass, String parentPath) {
        classNameValidator.validateBeanClass(enumClass);
        JSONTypeValidator.validateEnum(enumClass);
    }
}
