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

    @Property(name = "version")
    public String version;      // used to fast compare

    @Property(name = "services")
    public List<Service> services;

    @Property(name = "types")
    public List<APIType> types;

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
}
