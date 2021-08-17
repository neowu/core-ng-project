package core.framework.internal.web.api;

import core.framework.api.json.Property;

import java.util.ArrayList;
import java.util.List;

/**
 * @author neo
 */
public class APIDefinitionResponse {
    @Property(name = "app")
    public String app;

    @Property(name = "services")
    public List<Service> services;

    @Property(name = "types")
    public List<Type> types;

    public static class Service {
        @Property(name = "name")
        public String name;

        @Property(name = "operations")
        public List<Operation> operations;
    }

    public static class Operation {
        @Property(name = "name")
        public String name;

        @Property(name = "method")
        public String method;

        @Property(name = "path")
        public String path;

        @Property(name = "pathParams")
        public List<PathParam> pathParams = new ArrayList<>();

        @Property(name = "requestType")
        public String requestType;

        @Property(name = "responseType")
        public String responseType;

        @Property(name = "optional")
        public Boolean optional;

        @Property(name = "deprecated")
        public Boolean deprecated;
    }

    public static class PathParam {
        @Property(name = "name")
        public String name;

        @Property(name = "type")
        public String type;
    }

    public static class Type {
        @Property(name = "name")
        public String name;

        @Property(name = "type")
        public String type;     // bean or enum

        @Property(name = "fields")
        public List<Field> fields;

        @Property(name = "enumConstants")
        public List<EnumConstant> enumConstants;
    }

    public static class Field {
        @Property(name = "name")
        public String name;

        @Property(name = "type")
        public String type;

        @Property(name = "typeParams")
        public List<String> typeParams;

        @Property(name = "constraints")
        public Constraints constraints = new Constraints();
    }

    public static class Constraints {
        @Property(name = "notNull")
        public Boolean notNull;

        @Property(name = "notBlank")
        public Boolean notBlank;

        @Property(name = "min")
        public Double min;

        @Property(name = "max")
        public Double max;

        @Property(name = "size")
        public Size size;

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
