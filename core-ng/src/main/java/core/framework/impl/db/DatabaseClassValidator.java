package core.framework.impl.db;

import core.framework.api.json.Property;
import core.framework.db.Column;
import core.framework.db.DBEnumValue;
import core.framework.db.PrimaryKey;
import core.framework.db.Table;
import core.framework.impl.reflect.Classes;
import core.framework.impl.reflect.Fields;
import core.framework.impl.validate.type.DataTypeValidator;
import core.framework.impl.validate.type.TypeVisitor;
import core.framework.util.Sets;
import core.framework.util.Strings;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

import static core.framework.util.Strings.format;

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
        validator.visitor = this;
    }

    void validateEntityClass() {
        validator.validate();

        if (!foundPrimaryKey)
            throw new Error("db entity class must have @PrimaryKey, class=" + validator.type.getTypeName());
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
        }
    }

    @Override
    public void visitField(Field field, String parentPath) {
        Column column = field.getDeclaredAnnotation(Column.class);
        if (column == null)
            throw new Error("db entity field must have @Column, field=" + Fields.path(field));

        boolean added = columns.add(column.name());
        if (!added) {
            throw new Error(format("found duplicate column, field={}, column={}", Fields.path(field), column.name()));
        }

        PrimaryKey primaryKey = field.getDeclaredAnnotation(PrimaryKey.class);
        if (primaryKey != null) {
            foundPrimaryKey = true;
            validatePrimaryKey(primaryKey, field.getType(), field);
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

        if (!Strings.isBlank(primaryKey.sequence())) {
            if (foundSequencePrimaryKey) throw new Error("db entity must not have more than one sequence primary key, field=" + Fields.path(field));
            foundSequencePrimaryKey = true;
        }

        if ((foundAutoIncrementalPrimaryKey || foundSequencePrimaryKey)
                && !(Integer.class.equals(fieldClass) || Long.class.equals(fieldClass))) {
            throw new Error("auto increment or sequence primary key must be Integer or Long, field=" + Fields.path(field));
        }

        if (foundAutoIncrementalPrimaryKey && foundSequencePrimaryKey)
            throw new Error("db entity must not have both auto incremental and sequence primary key, field=" + Fields.path(field));
    }
}
