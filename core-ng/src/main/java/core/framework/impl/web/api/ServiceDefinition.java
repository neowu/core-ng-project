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
    public String name;
    public List<ServiceMethodDefinition> methods = Lists.newArrayList();

    public static class ServiceMethodDefinition {
        public String name;
        public HTTPMethod method;
        public String path;
        public String responseType;
        public Map<String, String> params = Maps.newLinkedHashMap();
    }
}
