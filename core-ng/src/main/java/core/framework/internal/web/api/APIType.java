package core.framework.internal.web.api;

import core.framework.api.json.Property;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * @author neo
 */
public class APIType {
    @Property(name = "name")
    public String name;

    @Property(name = "type")
    public String type;     // bean or enum

    @Property(name = "fields")
    public List<Field> fields;

    @Property(name = "enumConstants")
    public List<EnumConstant> enumConstants;

    public static class Field {
        @Property(name = "name")
        public String name;

        @Property(name = "type")
        public String type;

        @Nullable
        @Property(name = "typeParams")
        public List<String> typeParams;

        @Property(name = "constraints")
        public Constraints constraints = new Constraints();
    }

    public static class Constraints {
        @Nullable
        @Property(name = "notNull")
        public Boolean notNull;

        @Nullable
        @Property(name = "notBlank")
        public Boolean notBlank;

        @Nullable
        @Property(name = "min")
        public Double min;

        @Nullable
        @Property(name = "max")
        public Double max;

        @Nullable
        @Property(name = "size")
        public Size size;

        @Nullable
        @Property(name = "pattern")
        public String pattern;
    }

    public static class Size {
        @Property(name = "min")
        public Integer min;

        @Property(name = "max")
        public Integer max;
    }

    public static class EnumConstant {
        @Property(name = "name")
        public String name;

        @Property(name = "value")
        public String value;
    }
}
