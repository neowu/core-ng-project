package core.framework.impl.db;

import core.framework.api.db.Column;
import core.framework.api.db.EnumValue;
import core.framework.api.db.PrimaryKey;
import core.framework.api.db.Table;
import core.framework.api.util.Exceptions;
import core.framework.api.util.Sets;
import core.framework.impl.validate.type.DataTypeValidator;
import core.framework.impl.validate.type.TypeVisitor;

import javax.xml.bind.annotation.XmlAccessorType;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * @author neo
 */
final class DatabaseClassValidator implements TypeVisitor {
    private final DataTypeValidator validator;
    private boolean foundPK;
    private boolean validateView;
    private final Set<String> columns = Sets.newHashSet();

    DatabaseClassValidator(Class<?> entityClass) {
        validator = new DataTypeValidator(entityClass);
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
    public void visitClass(Class<?> instanceClass, boolean topLevel) {
        if (validateView) {
            if (instanceClass.isAnnotationPresent(Table.class))
                throw Exceptions.error("view class must not have @Table, lass={}", instanceClass.getCanonicalName());
        } else {
            if (!instanceClass.isAnnotationPresent(Table.class))
                throw Exceptions.error("entity class must have @Table, lass={}", instanceClass.getCanonicalName());
        }

        if (instanceClass.isAnnotationPresent(XmlAccessorType.class))
            throw Exceptions.error("entity class must not have jaxb annotation, please separate view class and entity class, class={}", instanceClass.getCanonicalName());
    }

    @Override
    public void visitField(Field field, boolean topLevel) {
        Class<?> fieldClass = field.getType();

        Column column = field.getDeclaredAnnotation(Column.class);
        if (column == null)
            throw Exceptions.error("field must have @Column, field={}", field);

        if (columns.contains(column.name())) {
            throw Exceptions.error("duplicated column found, column={}, field={}", column.name(), field);
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
                throw Exceptions.error("auto increment primary key must be Integer or Long, field={}", field);
            }
        }
    }

    private void validateEnumClass(Class<?> enumClass) {
        Enum[] constants = (Enum[]) enumClass.getEnumConstants();
        for (Enum constant : constants) {
            try {
                Field field = enumClass.getDeclaredField(constant.name());
                if (!field.isAnnotationPresent(EnumValue.class))
                    throw Exceptions.error("enum constant must have @EnumValue, enumClass={}, field={}", enumClass, field.getName());
            } catch (NoSuchFieldException e) {
                throw new Error(e);
            }
        }
    }
}
