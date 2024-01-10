package core.framework.internal.json;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import core.framework.api.json.Property;

import java.io.Serial;
import java.util.HashMap;
import java.util.Map;

/**
 * @author neo
 */
public class JSONAnnotationIntrospector extends AnnotationIntrospector {
    @Serial
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
    public String[] findEnumValues(MapperConfig<?> config, AnnotatedClass annotatedClass, Enum<?>[] enumValues, String[] names) {
        Map<String, String> mappings = null;
        for (AnnotatedField field : annotatedClass.fields()) {
            if (!field.getAnnotated().isEnumConstant()) continue;

            Property enumValue = field.getAnnotation(Property.class);
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
