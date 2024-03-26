package core.framework.internal.web.sys;

import core.framework.internal.module.PropertyManager;
import core.framework.internal.web.http.IPv4AccessControl;
import core.framework.web.Controller;
import core.framework.web.Request;
import core.framework.web.Response;

import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author neo
 */
public class PropertyController implements Controller {
    private final IPv4AccessControl accessControl = new IPv4AccessControl();
    private final PropertyManager propertyManager;

    public PropertyController(PropertyManager propertyManager) {
        this.propertyManager = propertyManager;
    }

    @Override
    public Response execute(Request request) {
        accessControl.validate(request.clientIP());
        return Response.text(properties());
    }

    String properties() {
        var builder = new StringBuilder(32768);
        builder.append("# properties\n");
        for (String key : new TreeSet<>(propertyManager.properties.keys())) {   // sort by key
            String value = propertyManager.property(key).orElse("");
            builder.append(key).append('=').append(mask(key, value)).append('\n');
        }
        builder.append("\n# system properties\n");
        Properties properties = System.getProperties();
        for (String key : new TreeSet<>(properties.stringPropertyNames())) { // sort by key
            String value = properties.getProperty(key);
            String maskedValue = mask(key, value);
            builder.append(key).append('=').append(maskedValue).append('\n');
        }
        builder.append("\n# env variables\n");
        Map<String, String> env = new TreeMap<>(System.getenv());   // sort by key
        for (Map.Entry<String, String> entry : env.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            builder.append(key).append('=').append(mask(key, value)).append('\n');
        }
        return builder.toString();
    }

    private String mask(String key, String value) {
        return propertyManager.maskValue(key, value);
    }
}
