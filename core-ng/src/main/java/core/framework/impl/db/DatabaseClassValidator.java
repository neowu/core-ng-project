package core.framework.impl.db;

import core.framework.api.json.Property;
import core.framework.db.Column;
import core.framework.db.DBEnumValue;
import core.framework.db.PrimaryKey;
import core.framework.db.Table;
import core.framework.impl.reflect.Enums;
import core.framework.impl.reflect.Fields;
import core.framework.impl.validate.type.DataTypeValidator;
import core.framework.impl.validate.type.TypeVisitor;
import core.framework.util.Exceptions;
import core.framework.util.Sets;
import core.framework.util.Strings;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Set;

/**
 * @author neo
 */
final class DatabaseClassValidator implements TypeVisitor {
    private final DataTypeValidator validator;
    private final Set<String> columns = Sets.newHashSet();
    private boolean foundPrimaryKey;
    private boolean foundAutoIncrementalPrimaryKey;
    private boolean foundSequencePrimaryKey;
    private boolean validateView;

    DatabaseClassValidator(Class<?> entityClass) {
        validator = new DataTypeValidator(entityClass);
        validator.allowedValueClass = this::allowedValueClass;
        validator.visitor = this;
    }

    void validateEntityClass() {
        validator.validate();

        if (!foundPrimaryKey)
            throw Exceptions.error("db entity class must have @PrimaryKey, class={}", validator.type.getTypeName());
    }

    void validateViewClass() {
        validateView = true;
        validator.validate();
    }

    private boolean allowedValueClass(Class<?> valueClass) {
        return String.class.equals(valueClass)
                || Integer.class.equals(valueClass)
                || Boolean.class.equals(valueClass)
                || Long.class.equals(valueClass)
                || Double.class.equals(valueClass)
                || BigDecimal.class.equals(valueClass)
                || LocalDate.class.equals(valueClass)
                || LocalDateTime.class.equals(valueClass)
                || ZonedDateTime.class.equals(valueClass)
                || valueClass.isEnum();
    }

    @Override
    public void visitClass(Class<?> objectClass, String path) {
        if (validateView) {
            if (objectClass.isAnnotationPresent(Table.class))
                throw Exceptions.error("db view class must not have @Table, class={}", objectClass.getCanonicalName());
        } else {
            if (!objectClass.isAnnotationPresent(Table.class))
                throw Exceptions.error("db entity class must have @Table, class={}", objectClass.getCanonicalName());
        }
    }

    @Override
    public void visitField(Field field, String parentPath) {
        Class<?> fieldClass = field.getType();

        Column column = field.getDeclaredAnnotation(Column.class);
        if (column == null)
            throw Exceptions.error("db entity field must have @Column, field={}", Fields.path(field));

        if (columns.contains(column.name())) {
            throw Exceptions.error("found duplicate column, field={}, column={}", Fields.path(field), column.name());
        } else {
            columns.add(column.name());
        }

        if (fieldClass.isEnum()) {
            validateEnumClass(fieldClass, field);
        }

        PrimaryKey primaryKey = field.getDeclaredAnnotation(PrimaryKey.class);
        if (primaryKey != null) {
            foundPrimaryKey = true;

            validatePrimaryKey(primaryKey, fieldClass, field);
        }
    }

    private void validatePrimaryKey(PrimaryKey primaryKey, Class<?> fieldClass, Field field) {
        if (primaryKey.autoIncrement()) {
            if (foundAutoIncrementalPrimaryKey) throw Exceptions.error("db entity must not have more than one auto incremental primary key, field={}", Fields.path(field));
            foundAutoIncrementalPrimaryKey = true;

            if (!(Integer.class.equals(fieldClass) || Long.class.equals(fieldClass))) {
                throw Exceptions.error("auto increment primary key must be Integer or Long, field={}", Fields.path(field));
            }
        }

        if (!Strings.isEmpty(primaryKey.sequence())) {
            if (foundSequencePrimaryKey) throw Exceptions.error("db entity must not have more than one sequence primary key, field={}", Fields.path(field));
            foundSequencePrimaryKey = true;

            if (!(Integer.class.equals(fieldClass) || Long.class.equals(fieldClass))) {
                throw Exceptions.error("sequence primary key must be Integer or Long, field={}", Fields.path(field));
            }

            if (primaryKey.autoIncrement()) {
                throw Exceptions.error("primary key must be either auto increment or sequence, field={}", Fields.path(field));
            }
        }

        if (foundAutoIncrementalPrimaryKey && foundSequencePrimaryKey)
            throw Exceptions.error("db entity must not have both auto incremental and sequence primary key, field={}", Fields.path(field));
    }

    private void validateEnumClass(Class<?> enumClass, Field field) {
        Enum<?>[] constants = (Enum<?>[]) enumClass.getEnumConstants();
        Set<String> enumValues = Sets.newHashSet();
        for (Enum<?> constant : constants) {
            DBEnumValue enumValue = Enums.constantAnnotation(constant, DBEnumValue.class);
            if (enumValue == null) {
                throw Exceptions.error("db enum must have @DBEnumValue, field={}, enum={}", Fields.path(field), Enums.path(constant));
            }
            boolean added = enumValues.add(enumValue.value());
            if (!added) {
                throw Exceptions.error("db enum value must be unique, enum={}, value={}", Enums.path(constant), enumValue.value());
            }
            Property property = Enums.constantAnnotation(constant, Property.class);
            if (property != null) {
                throw Exceptions.error("db enum must not have json annotation, please separate view and entity, field={}, enum={}", Fields.path(field), Enums.path(constant));
            }
        }
    }
}
