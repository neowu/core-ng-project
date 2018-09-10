package core.framework.mongo.impl;

import core.framework.api.json.Property;
import core.framework.impl.reflect.Classes;
import core.framework.impl.reflect.Fields;
import core.framework.impl.validate.type.DataTypeValidator;
import core.framework.impl.validate.type.TypeVisitor;
import core.framework.mongo.Collection;
import core.framework.mongo.Id;
import core.framework.mongo.MongoEnumValue;
import core.framework.util.Maps;
import core.framework.util.Sets;
import org.bson.types.ObjectId;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public final class MongoClassValidator implements TypeVisitor {
    private final DataTypeValidator validator;
    private final Map<String, Set<String>> fields = Maps.newHashMap();
    private boolean validateView;
    private Field id;

    MongoClassValidator(Class<?> entityClass) {
        validator = new DataTypeValidator(entityClass);
        validator.allowedValueClasses = Set.of(ObjectId.class, String.class, Boolean.class,
                Integer.class, Long.class, Double.class,
                LocalDateTime.class, ZonedDateTime.class);
        validator.allowChild = true;
        validator.visitor = this;
    }

    void validateEntityClass() {
        validator.validate();

        if (id == null) {
            throw new Error(format("mongo entity class must have @Id field, class={}", validator.type.getTypeName()));
        }
    }

    void validateViewClass() {
        validateView = true;
        validator.validate();
    }

    @Override
    public void visitClass(Class<?> objectClass, String path) {
        if (!validateView && path == null && !objectClass.isAnnotationPresent(Collection.class))
            throw new Error(format("mongo entity class must have @Collection, class={}", objectClass.getCanonicalName()));
    }

    @Override
    public void visitField(Field field, String parentPath) {
        if (field.isAnnotationPresent(Id.class)) {
            validateId(field, parentPath == null);
        } else {
            core.framework.mongo.Field mongoField = field.getDeclaredAnnotation(core.framework.mongo.Field.class);
            if (mongoField == null)
                throw new Error(format("mongo entity field must have @Field, field={}", Fields.path(field)));
            String mongoFieldName = mongoField.name();

            Set<String> fields = this.fields.computeIfAbsent(parentPath, key -> Sets.newHashSet());
            if (fields.contains(mongoFieldName)) {
                throw new Error(format("found duplicate field, field={}, mongoField={}", Fields.path(field), mongoFieldName));
            }
            fields.add(mongoFieldName);
        }
    }

    @Override
    public void visitEnum(Class<?> enumClass, String parentPath) {
        Set<String> enumValues = Sets.newHashSet();
        List<Field> fields = Classes.enumConstantFields(enumClass);
        for (Field field : fields) {
            MongoEnumValue enumValue = field.getDeclaredAnnotation(MongoEnumValue.class);
            if (enumValue == null) {
                throw new Error(format("mongo enum must have @MongoEnumValue, field={}", Fields.path(field)));
            }
            boolean added = enumValues.add(enumValue.value());
            if (!added) {
                throw new Error(format("mongo enum value must be unique, field={}, value={}", Fields.path(field), enumValue.value()));
            }
            Property property = field.getDeclaredAnnotation(Property.class);
            if (property != null) {
                throw new Error(format("mongo enum must not have json annotation, please separate view and entity, field={}", Fields.path(field)));
            }
        }
    }

    private void validateId(Field field, boolean topLevel) {
        if (topLevel) {
            if (id != null)
                throw new Error(format("mongo entity class must have only one @Id field, previous={}, current={}", Fields.path(id), Fields.path(field)));
            Class<?> fieldClass = field.getType();
            if (!ObjectId.class.equals(fieldClass) && !String.class.equals(fieldClass)) {
                throw new Error(format("@Id field must be either ObjectId or String, field={}, class={}", Fields.path(field), fieldClass.getCanonicalName()));
            }
            id = field;
        } else {
            throw new Error(format("mongo nested entity class must not have @Id field, field={}", Fields.path(field)));
        }
    }
}
