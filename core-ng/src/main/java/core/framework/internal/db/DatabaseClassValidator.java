package core.framework.internal.db;

import core.framework.api.json.Property;
import core.framework.api.validate.NotNull;
import core.framework.db.Column;
import core.framework.db.DBEnumValue;
import core.framework.db.PrimaryKey;
import core.framework.db.Table;
import core.framework.internal.json.JSONClassValidator;
import core.framework.internal.reflect.Classes;
import core.framework.internal.reflect.Fields;
import core.framework.internal.reflect.GenericTypes;
import core.framework.internal.validate.ClassValidatorSupport;
import core.framework.util.Sets;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
final class DatabaseClassValidator {
    private final ClassValidatorSupport support = new ClassValidatorSupport();
    private final Class<?> entityClass;
    private final Set<Class<?>> allowedValueClasses = Set.of(String.class, Boolean.class,
        Integer.class, Long.class, Double.class, BigDecimal.class,
        LocalDate.class, LocalDateTime.class, ZonedDateTime.class);
    private final Set<String> columns = Sets.newHashSet();
    private final boolean isView;
    private boolean foundPrimaryKey;
    private boolean foundAutoIncrementalPrimaryKey;
    private Object defaultObject;

    DatabaseClassValidator(Class<?> entityClass, boolean isView) {
        this.entityClass = entityClass;
        this.isView = isView;
    }

    void validate() {
        support.validateClass(entityClass);

        boolean foundTable = entityClass.isAnnotationPresent(Table.class);
        if (isView && foundTable)
            throw new Error("db view class must not have @Table, class=" + entityClass.getCanonicalName());
        else if (!isView && !foundTable)
            throw new Error("db entity class must have @Table, class=" + entityClass.getCanonicalName());

        initializeDefaultObject();

        validateFields();

        if (!isView && !foundPrimaryKey)
            throw new Error("db entity class must have @PrimaryKey, class=" + entityClass.getCanonicalName());
    }

    private void initializeDefaultObject() {
        try {
            defaultObject = entityClass.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new Error(e);
        }
    }

    private void validateFields() {
        for (Field field : support.declaredFields(entityClass)) {
            Column column = validateAnnotations(field);
            if (column.json()) {
                validateJSONField(field);
            } else {
                validateValueField(field);
            }

            try {
                // entity constructed by "new" with default value will break partialUpdate accidentally, due to fields are not null will be updated to db
                if (!isView && field.get(defaultObject) != null)
                    throw new Error("db entity field must not have default value, field=" + Fields.path(field));
            } catch (ReflectiveOperationException e) {
                throw new Error(e);
            }
        }
    }

    private Column validateAnnotations(Field field) {
        Column column = field.getDeclaredAnnotation(Column.class);
        if (column == null) throw new Error("db entity field must have @Column, field=" + Fields.path(field));

        Property property = field.getDeclaredAnnotation(Property.class);
        if (property != null)
            throw new Error("db entity field must not have json annotation, please separate view and entity, field=" + Fields.path(field));

        boolean added = columns.add(column.name());
        if (!added) {
            throw new Error(format("found duplicate column, field={}, column={}", Fields.path(field), column.name()));
        }

        PrimaryKey primaryKey = field.getDeclaredAnnotation(PrimaryKey.class);
        if (primaryKey != null) {
            if (isView) throw new Error("db view field must not have @PrimaryKey, field=" + Fields.path(field));
            foundPrimaryKey = true;
            validatePrimaryKey(primaryKey, field.getType(), field);
        }

        return column;
    }

    private void validateJSONField(Field field) {
        Type fieldType = field.getGenericType();
        if (GenericTypes.isList(fieldType)) {
            if (!GenericTypes.isGenericList(fieldType))
                throw new Error("db json list field must be List<T> and T must be enum or value class, field=" + Fields.path(field));

            Class<?> valueClass = GenericTypes.listValueClass(fieldType);
            if (valueClass.isEnum()) {
                JSONClassValidator.validateEnum(valueClass);
                return;
            }
            // use db.allowedValues not json.allowedValues to keep json db field consistent with db entity
            if (allowedValueClasses.contains(valueClass)) return;

            throw new Error("db json list field must be List<T> and T must be enum or value class, field=" + Fields.path(field));
        } else {
            Class<?> fieldClass = GenericTypes.rawClass(fieldType);
            if (fieldClass.isEnum() || allowedValueClasses.contains(fieldClass))
                throw new Error("db json field must be bean or list, field=" + Fields.path(field));
            new JSONClassValidator(fieldClass).validate();
        }
    }

    private void validateValueField(Field field) {
        Class<?> fieldClass = field.getType();

        if (fieldClass.isEnum()) {  // enum is allowed value type
            validateEnum(fieldClass);
            return;
        }

        if (allowedValueClasses.contains(fieldClass)) return;

        if (fieldClass.getPackageName().startsWith("java"))
            throw new Error(format("field class is not supported, class={}, field={}", fieldClass.getCanonicalName(), Fields.path(field)));

        throw new Error(format("child object is not allowed, do you mean @Column(json=true), class={}, field={}", fieldClass.getCanonicalName(), Fields.path(field)));
    }

    private void validatePrimaryKey(PrimaryKey primaryKey, Class<?> fieldClass, Field field) {
        if (primaryKey.autoIncrement()) {
            if (foundAutoIncrementalPrimaryKey) throw new Error("db entity must not have more than one auto incremental primary key, field=" + Fields.path(field));
            foundAutoIncrementalPrimaryKey = true;
        }

        if (foundAutoIncrementalPrimaryKey && !(Integer.class.equals(fieldClass) || Long.class.equals(fieldClass))) {
            throw new Error("auto increment or sequence primary key must be Integer or Long, field=" + Fields.path(field));
        }

        NotNull notNull = field.getDeclaredAnnotation(NotNull.class);
        if (notNull != null)
            throw new Error("db @PrimaryKey field must not have @NotNull, field=" + Fields.path(field));
    }

    private void validateEnum(Class<?> enumClass) {
        Set<String> enumValues = Sets.newHashSet();
        List<Field> fields = Classes.enumConstantFields(enumClass);
        for (Field field : fields) {
            DBEnumValue enumValue = field.getDeclaredAnnotation(DBEnumValue.class);
            if (enumValue == null)
                throw new Error("db enum must have @DBEnumValue, field=" + Fields.path(field));

            boolean added = enumValues.add(enumValue.value());
            if (!added)
                throw new Error(format("found duplicate db enum value, field={}, value={}", Fields.path(field), enumValue.value()));

            Property property = field.getDeclaredAnnotation(Property.class);
            if (property != null)
                throw new Error("db enum must not have json annotation, please separate view and entity, field=" + Fields.path(field));
        }
    }
}
