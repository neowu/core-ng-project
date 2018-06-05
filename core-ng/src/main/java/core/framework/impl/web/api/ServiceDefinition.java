package core.framework.impl.web.api;

import core.framework.http.HTTPMethod;
import core.framework.util.Lists;
import core.framework.util.Maps;

import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public class ServiceDefinition {
    public final List<Operation> operations = Lists.newArrayList();
    public String name;

    public static class Operation {
        public final Map<String, String> pathParams = Maps.newLinkedHashMap();
        public String name;
        public HTTPMethod method;
        public String path;
        public String responseType;
        public String requestType;
    }
}
