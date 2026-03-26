package core.framework.internal.json;

import core.framework.api.json.Property;
import tools.jackson.core.Version;
import tools.jackson.databind.AnnotationIntrospector;
import tools.jackson.databind.PropertyName;
import tools.jackson.databind.cfg.MapperConfig;
import tools.jackson.databind.introspect.Annotated;
import tools.jackson.databind.introspect.AnnotatedClass;
import tools.jackson.databind.introspect.AnnotatedField;

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
    public PropertyName findNameForSerialization(MapperConfig<?> config, Annotated annotated) {
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
    public PropertyName findNameForDeserialization(MapperConfig<?> config, Annotated annotated) {
        return propertyName(annotated);
    }

    private PropertyName propertyName(Annotated annotated) {
        Property element = annotated.getAnnotation(Property.class);
        if (element != null) {
            return new PropertyName(element.name());
        }
        return null;
    }
}
