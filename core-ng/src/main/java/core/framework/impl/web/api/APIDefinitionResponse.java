package core.framework.impl.web.api;

import core.framework.api.json.Property;

import java.util.List;

/**
 * @author neo
 */
public class APIDefinitionResponse {
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
        public List<PathParam> pathParams;

        @Property(name = "responseType")
        public String responseType;

        @Property(name = "requestType")
        public String requestType;
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
        public String type;

        @Property(name = "definition")
        public String definition;
    }
}
