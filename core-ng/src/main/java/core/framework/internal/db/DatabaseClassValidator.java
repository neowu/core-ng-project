package core.framework.internal.db;

import core.framework.api.json.Property;
import core.framework.api.validate.NotNull;
import core.framework.db.Column;
import core.framework.db.DBEnumValue;
import core.framework.db.PrimaryKey;
import core.framework.db.Table;
import core.framework.internal.reflect.Classes;
import core.framework.internal.reflect.Fields;
import core.framework.internal.validate.ClassValidator;
import core.framework.internal.validate.ClassVisitor;
import core.framework.util.Sets;

import java.lang.reflect.Field;
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
final class DatabaseClassValidator implements ClassVisitor {
    private final ClassValidator validator;
    private final Set<String> columns = Sets.newHashSet();
    private boolean foundPrimaryKey;
    private boolean foundAutoIncrementalPrimaryKey;
    private boolean validateView;
    private Object entityWithDefaultValue;

    DatabaseClassValidator(Class<?> entityClass) {
        validator = new ClassValidator(entityClass);
        validator.allowedValueClasses = Set.of(String.class, Boolean.class,
            Integer.class, Long.class, Double.class, BigDecimal.class,
            LocalDate.class, LocalDateTime.class, ZonedDateTime.class);
        validator.visitor = this;
    }

    void validateEntityClass() {
        validator.validate();

        if (!foundPrimaryKey)
            throw new Error("db entity class must have @PrimaryKey, class=" + validator.instanceClass.getCanonicalName());
    }

    void validateViewClass() {
        validateView = true;
        validator.validate();
    }

    @Override
    public void visitClass(Class<?> objectClass, String path) {
        if (validateView) {
            if (objectClass.isAnnotationPresent(Table.class))
                throw new Error("db view class must not have @Table, class=" + objectClass.getCanonicalName());
        } else {
            if (!objectClass.isAnnotationPresent(Table.class))
                throw new Error("db entity class must have @Table, class=" + objectClass.getCanonicalName());

            try {
                entityWithDefaultValue = objectClass.getDeclaredConstructor().newInstance();
            } catch (ReflectiveOperationException e) {
                throw new Error(e);
            }
        }
    }

    @Override
    public void visitField(Field field, String parentPath) {
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
            if (validateView) throw new Error("db view field must not have @PrimaryKey, field=" + Fields.path(field));
            foundPrimaryKey = true;
            validatePrimaryKey(primaryKey, field.getType(), field);
        }

        try {
            // entity constructed by "new" with default value will break partialUpdate accidentally, due to fields are not null will be updated to db
            if (!validateView && field.get(entityWithDefaultValue) != null)
                throw new Error("db entity field must not have default value, field=" + Fields.path(field));
        } catch (ReflectiveOperationException e) {
            throw new Error(e);
        }
    }

    @Override
    public void visitEnum(Class<?> enumClass) {
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
}
