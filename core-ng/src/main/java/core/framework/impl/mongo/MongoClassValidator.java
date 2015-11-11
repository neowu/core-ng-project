package core.framework.impl.mongo;

import core.framework.api.mongo.Collection;
import core.framework.api.mongo.Id;
import core.framework.api.util.Exceptions;
import core.framework.api.util.Maps;
import core.framework.api.util.Sets;
import core.framework.impl.reflect.Fields;
import core.framework.impl.validate.type.TypeValidator;
import core.framework.impl.validate.type.TypeVisitor;
import org.bson.types.ObjectId;

import javax.xml.bind.annotation.XmlEnumValue;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

/**
 * @author neo
 */
public class MongoClassValidator implements TypeVisitor {
    private final TypeValidator validator;
    private final Map<String, Set<String>> fields = Maps.newHashMap();
    private boolean validateView;
    private Field id;

    public MongoClassValidator(Class<?> entityClass) {
        validator = new TypeValidator(entityClass);
        validator.allowedValueClass = this::allowedValueClass;
        validator.allowChildListAndMap = true;
        validator.allowChildObject = true;
        validator.visitor = this;
    }

    public void validateEntityClass() {
        validator.validate();

        if (id == null) {
            throw Exceptions.error("mongo entity class must have @Id field, class={}", validator.type.getTypeName());
        }
    }

    public void validateViewClass() {
        validateView = true;
        validator.validate();
    }

    private boolean allowedValueClass(Class<?> valueClass) {
        return String.class.equals(valueClass)
            || ObjectId.class.equals(valueClass)
            || Integer.class.equals(valueClass)
            || Boolean.class.equals(valueClass)
            || Long.class.equals(valueClass)
            || Double.class.equals(valueClass)
            || LocalDateTime.class.equals(valueClass)
            || Enum.class.isAssignableFrom(valueClass);
    }

    @Override
    public void visitClass(Class<?> objectClass, String path) {
        if (!validateView && path == null && !objectClass.isAnnotationPresent(Collection.class))
            throw Exceptions.error("mongo entity class must have @Collection, class={}", objectClass.getCanonicalName());
    }

    @Override
    public void visitField(Field field, String parentPath) {
        if (field.isAnnotationPresent(Id.class)) {
            validateId(field, parentPath == null);
        } else {
            core.framework.api.mongo.Field mongoField = field.getDeclaredAnnotation(core.framework.api.mongo.Field.class);
            if (mongoField == null)
                throw Exceptions.error("mongo entity field must have @Field, field={}", Fields.path(field));
            String mongoFieldName = mongoField.name();

            Set<String> fields = this.fields.computeIfAbsent(parentPath, key -> Sets.newHashSet());
            if (fields.contains(mongoFieldName)) {
                throw Exceptions.error("duplicated field found, field={}, mongoField={}", Fields.path(field), mongoFieldName);
            }
            fields.add(mongoFieldName);

            Class<?> fieldClass = field.getType();
            if (Enum.class.isAssignableFrom(fieldClass)) {
                validateEnumClass(fieldClass, field);
            }
        }
    }

    private void validateEnumClass(Class<?> enumClass, Field field) {
        Enum[] constants = (Enum[]) enumClass.getEnumConstants();
        for (Enum constant : constants) {
            try {
                Field enumField = enumClass.getDeclaredField(constant.name());
                if (enumField.isAnnotationPresent(XmlEnumValue.class))
                    throw Exceptions.error("mongo enum must not have jaxb annotation, please separate view and entity, field={}, enum={}", Fields.path(field), Fields.path(enumField));
            } catch (NoSuchFieldException e) {
                throw new Error(e);
            }
        }
    }

    private void validateId(Field field, boolean topLevel) {
        if (topLevel) {
            if (id != null)
                throw Exceptions.error("mongo entity class must have only one @Id field, previous={}, current={}", Fields.path(id), Fields.path(field));
            Class<?> fieldClass = field.getType();
            if (!ObjectId.class.equals(fieldClass) && !String.class.equals(fieldClass)) {
                throw Exceptions.error("@Id field must be either ObjectId or String, field={}, class={}", Fields.path(field), fieldClass.getCanonicalName());
            }
            id = field;
        } else {
            throw Exceptions.error("mongo nested entity class must not have @Id field, field={}", field);
        }
    }
}
