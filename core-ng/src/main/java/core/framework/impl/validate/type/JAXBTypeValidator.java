package core.framework.impl.validate.type;

import core.framework.api.util.Exceptions;
import core.framework.api.util.Maps;
import core.framework.api.util.Sets;
import core.framework.impl.reflect.Fields;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlTransient;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

/**
 * @author neo
 */
public class JAXBTypeValidator implements TypeVisitor {
    public static void validateEnumClass(Class<? extends Enum> enumClass) {
        if (enumClass.isAnnotationPresent(XmlEnum.class))
            throw Exceptions.error("enum class must not have @XmlEnum, enumClass={}", enumClass.getCanonicalName());

        Enum[] constants = enumClass.getEnumConstants();
        for (Enum constant : constants) {
            try {
                Field enumField = enumClass.getField(constant.name());
                if (!enumField.isAnnotationPresent(XmlEnumValue.class)) {
                    throw Exceptions.error("enum must have @XmlEnumValue, enum={}", Fields.path(enumField));
                }
            } catch (NoSuchFieldException e) {
                throw new Error(e);
            }
        }
    }
    protected final TypeValidator validator;
    private final Map<String, Set<String>> elements = Maps.newHashMap();

    protected JAXBTypeValidator(Type instanceType) {
        validator = new TypeValidator(instanceType);
        validator.allowedValueClass = this::allowedValueClass;
        validator.allowChildListAndMap = true;
        validator.allowChildObject = true;
        validator.visitor = this;
    }

    public void validate() {
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
            || Instant.class.equals(valueClass)
            || Enum.class.isAssignableFrom(valueClass);
    }

    @Override
    public void visitClass(Class<?> objectClass, String path) {
        XmlAccessorType accessorType = objectClass.getDeclaredAnnotation(XmlAccessorType.class);
        if (accessorType == null || accessorType.value() != XmlAccessType.FIELD)
            throw Exceptions.error("class must have @XmlAccessorType(XmlAccessType.FIELD), class={}", objectClass.getCanonicalName());
    }

    @Override
    public void visitField(Field field, String parentPath) {
        XmlElement element = field.getDeclaredAnnotation(XmlElement.class);
        if (element == null)
            throw Exceptions.error("field must have @XmlElement(name=), field={}", Fields.path(field));

        if (field.isAnnotationPresent(XmlTransient.class))
            throw Exceptions.error("field must not have @XmlTransient, field={}", Fields.path(field));

        String name = element.name();

        if ("##default".equals(name)) {
            throw Exceptions.error("@XmlElement must have name attribute, field={}", Fields.path(field));
        }

        Set<String> elements = this.elements.computeIfAbsent(parentPath, key -> Sets.newHashSet());
        if (elements.contains(name)) {
            throw Exceptions.error("element is duplicated, field={}, name={}", Fields.path(field), name);
        }
        elements.add(name);

        if (Enum.class.isAssignableFrom(field.getType())) {
            @SuppressWarnings("unchecked")
            Class<? extends Enum> enumClass = (Class<? extends Enum>) field.getType();
            validateEnumClass(enumClass);
        }
    }
}