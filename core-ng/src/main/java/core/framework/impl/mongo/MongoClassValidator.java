package core.framework.impl.mongo;

import core.framework.api.mongo.Collection;
import core.framework.api.mongo.Id;
import core.framework.api.util.Exceptions;
import core.framework.api.util.Maps;
import core.framework.api.util.Sets;
import core.framework.impl.validate.type.TypeValidator;
import core.framework.impl.validate.type.TypeVisitor;
import org.bson.types.ObjectId;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

/**
 * @author neo
 */
public class MongoClassValidator implements TypeVisitor {
    private final TypeValidator validator;
    private boolean validateView;
    private final Map<Class, Set<String>> fields = Maps.newHashMap();
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
            throw Exceptions.error("entity class must have @Id field, class={}", validator.type.getTypeName());
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
    public void visitClass(Class<?> objectClass, boolean topLevel) {
        if (!validateView && topLevel && !objectClass.isAnnotationPresent(Collection.class))
            throw Exceptions.error("entity class must have @Collection, class={}", objectClass.getCanonicalName());
    }

    @Override
    public void visitField(Field field, boolean topLevel) {
        if (field.isAnnotationPresent(Id.class)) {
            validateId(field, topLevel);
        } else {
            core.framework.api.mongo.Field mongoField = field.getDeclaredAnnotation(core.framework.api.mongo.Field.class);
            if (mongoField == null) throw Exceptions.error("field must have @Field, field={}", field);
            String mongoFieldName = mongoField.name();

            Set<String> fields = this.fields.computeIfAbsent(field.getDeclaringClass(), key -> Sets.newHashSet());
            if (fields.contains(mongoFieldName)) {
                throw Exceptions.error("field is duplicated, field={}, mongoField={}", field, mongoFieldName);
            }
            fields.add(mongoFieldName);
        }
    }

    private void validateId(Field field, boolean topLevel) {
        if (topLevel) {
            if (id != null)
                throw Exceptions.error("entity class must have only one @Id field, previous={}, current={}", id, field);
            if (!ObjectId.class.equals(field.getType()) || !"id".equals(field.getName()))
                throw Exceptions.error("@Id must be \"public org.bson.types.ObjectId id;\", field={}", field);
            id = field;
        } else {
            throw Exceptions.error("child class must not have @Id field, field={}", field);
        }
    }
}
