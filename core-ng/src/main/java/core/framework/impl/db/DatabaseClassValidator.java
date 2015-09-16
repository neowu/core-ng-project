package core.framework.impl.db;

import core.framework.api.db.Column;
import core.framework.api.db.EnumValue;
import core.framework.api.db.PrimaryKey;
import core.framework.api.db.Table;
import core.framework.api.util.Exceptions;
import core.framework.api.util.Sets;
import core.framework.impl.reflect.Fields;
import core.framework.impl.validate.type.TypeValidator;
import core.framework.impl.validate.type.TypeVisitor;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlEnumValue;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * @author neo
 */
final class DatabaseClassValidator implements TypeVisitor {
    private final TypeValidator validator;
    private boolean foundPK;
    private boolean validateView;
    private final Set<String> columns = Sets.newHashSet();

    DatabaseClassValidator(Class<?> entityClass) {
        validator = new TypeValidator(entityClass);
        validator.allowedValueClass = this::allowedValueClass;
        validator.visitor = this;
    }

    public void validateEntityClass() {
        validator.validate();

        if (!foundPK)
            throw Exceptions.error("entity class must have @PrimaryKey, class={}", validator.type.getTypeName());
    }

    public void validateViewClass() {
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
            || LocalDateTime.class.equals(valueClass)
            || Enum.class.isAssignableFrom(valueClass);
    }

    @Override
    public void visitClass(Class<?> objectClass, String path) {
        if (validateView) {
            if (objectClass.isAnnotationPresent(Table.class))
                throw Exceptions.error("view class must not have @Table, class={}", objectClass.getCanonicalName());
        } else {
            if (!objectClass.isAnnotationPresent(Table.class))
                throw Exceptions.error("entity class must have @Table, class={}", objectClass.getCanonicalName());
        }

        if (objectClass.isAnnotationPresent(XmlAccessorType.class))
            throw Exceptions.error("entity class must not have jaxb annotation, please separate view class and entity class, class={}", objectClass.getCanonicalName());
    }

    @Override
    public void visitField(Field field, String parentPath) {
        Class<?> fieldClass = field.getType();

        Column column = field.getDeclaredAnnotation(Column.class);
        if (column == null)
            throw Exceptions.error("field must have @Column, field={}", Fields.path(field));

        if (columns.contains(column.name())) {
            throw Exceptions.error("duplicated column found, column={}, field={}", column.name(), Fields.path(field));
        } else {
            columns.add(column.name());
        }

        if (Enum.class.isAssignableFrom(fieldClass)) {
            validateEnumClass(fieldClass);
        }

        PrimaryKey primaryKey = field.getDeclaredAnnotation(PrimaryKey.class);
        if (primaryKey != null) {
            foundPK = true;
            if (primaryKey.autoIncrement() && !(Integer.class.equals(fieldClass) || Long.class.equals(fieldClass))) {
                throw Exceptions.error("auto increment primary key must be Integer or Long, field={}", Fields.path(field));
            }
        }
    }

    private void validateEnumClass(Class<?> enumClass) {
        Enum[] constants = (Enum[]) enumClass.getEnumConstants();
        for (Enum constant : constants) {
            try {
                Field enumField = enumClass.getDeclaredField(constant.name());
                if (!enumField.isAnnotationPresent(EnumValue.class))
                    throw Exceptions.error("db enum must have @EnumValue, enum={}", Fields.path(enumField));

                if (enumField.isAnnotationPresent(XmlEnumValue.class))
                    throw Exceptions.error("db enum must not have jaxb annotation, please separate view and entity, enum={}", Fields.path(enumField));

            } catch (NoSuchFieldException e) {
                throw new Error(e);
            }
        }
    }
}
