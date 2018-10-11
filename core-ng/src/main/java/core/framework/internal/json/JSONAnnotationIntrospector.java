package core.framework.internal.json;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import core.framework.api.json.Property;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author neo
 */
public class JSONAnnotationIntrospector extends AnnotationIntrospector {
    private static final long serialVersionUID = 3638140740110527623L;

    @Override
    public Version version() {
        return Version.unknownVersion();
    }

    @Override
    public VisibilityChecker<?> findAutoDetectVisibility(AnnotatedClass annotatedClass, VisibilityChecker<?> checker) {
        return checker.withFieldVisibility(Visibility.PUBLIC_ONLY)
                      .withSetterVisibility(Visibility.NONE)
                      .withGetterVisibility(Visibility.NONE)
                      .withIsGetterVisibility(Visibility.NONE);
    }

    @Override
    public PropertyName findNameForSerialization(Annotated annotated) {
        return propertyName(annotated);
    }

    @Override
    public String[] findEnumValues(Class<?> enumType, Enum<?>[] enumValues, String[] names) {
        Map<String, String> mappings = null;
        for (Field field : enumType.getDeclaredFields()) {
            if (!field.isEnumConstant()) continue;

            Property enumValue = field.getDeclaredAnnotation(Property.class);
            if (enumValue == null) continue;

            String value = enumValue.name();
            if (value.isEmpty()) continue;

            if (mappings == null) mappings = new HashMap<>();
            mappings.put(field.getName(), value);
        }

        if (mappings != null) {
            int length = enumValues.length;
            for (int i = 0; i < length; i++) {
                String name = enumValues[i].name();
                String value = mappings.get(name);
                if (value != null) names[i] = value;
            }
        }
        return names;
    }

    @Override
    public PropertyName findNameForDeserialization(Annotated annotated) {
        return propertyName(annotated);
    }

    private PropertyName propertyName(Annotated annotated) {
        Property element = annotated.getAnnotation(Property.class);
        if (element != null) {
            return new PropertyName(element.name(), null);
        }
        return null;
    }
}
