package core.framework.impl.web.management;

import core.framework.impl.module.PropertyManager;
import core.framework.impl.web.http.IPAccessControl;
import core.framework.web.Controller;
import core.framework.web.Request;
import core.framework.web.Response;

import java.util.Set;
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
        return Response.text(text());
    }

    String text() {
        StringBuilder builder = new StringBuilder();
        Set<String> keys = new TreeSet<>(propertyManager.properties.keys());   // sort by key
        for (String key : keys) {
            String value = propertyManager.property(key).orElse("");
            builder.append(key).append('=').append(propertyManager.maskValue(key, value)).append('\n');
        }
        return builder.toString();
    }
}
