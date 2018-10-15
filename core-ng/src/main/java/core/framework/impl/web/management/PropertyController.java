package core.framework.impl.web.management;

import core.framework.impl.module.PropertyManager;
import core.framework.impl.web.http.IPAccessControl;
import core.framework.web.Controller;
import core.framework.web.Request;
import core.framework.web.Response;

import java.util.Properties;
import java.util.TreeSet;

/**
 * @author neo
 */
public class PropertyController implements Controller {
    private final IPAccessControl accessControl = new IPAccessControl();
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
            builder.append(key).append('=').append(propertyManager.maskValue(key, value)).append('\n');
        }
        builder.append("\n# system properties\n");
        Properties properties = System.getProperties();
        for (var key : new TreeSet<>(properties.stringPropertyNames())) { // sort by key
            String value = properties.getProperty(key);
            builder.append(key).append('=').append(value).append('\n');
        }
        return builder.toString();
    }
}
