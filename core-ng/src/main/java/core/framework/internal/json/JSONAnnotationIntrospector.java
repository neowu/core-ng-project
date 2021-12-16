package core.framework.internal.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.introspect.Annotated;
import core.framework.api.json.IgnoreNull;
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

    @Override
    public JsonInclude.Value findPropertyInclusion(Annotated a) {
        return propertyInclusion(a);
    }

    private JsonInclude.Value propertyInclusion(Annotated annotated) {
        IgnoreNull ignoreNull = annotated.getAnnotation(IgnoreNull.class);
        if (ignoreNull != null) {
            return JsonInclude.Value.empty().withValueInclusion(JsonInclude.Include.NON_NULL);
        }
        return null;
    }
}
