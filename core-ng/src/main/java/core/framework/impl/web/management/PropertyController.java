package core.framework.impl.web.management;

import core.framework.impl.module.PropertyManager;
import core.framework.web.Controller;
import core.framework.web.Request;
import core.framework.web.Response;

import java.util.List;

/**
 * @author neo
 */
public class PropertyController implements Controller {
    private final PropertyManager propertyManager;

    public PropertyController(PropertyManager propertyManager) {
        this.propertyManager = propertyManager;
    }

    @Override
    public Response execute(Request request) {
        ControllerHelper.validateFromLocalNetwork(request.clientIP());
        return Response.text(text());
    }

    String text() {
        StringBuilder builder = new StringBuilder();
        List<PropertyManager.PropertyEntry> entries = propertyManager.entries();
        for (PropertyManager.PropertyEntry entry : entries) {
            if (entry.source != PropertyManager.PropertySource.PROPERTY_FILE) builder.append("# ").append(entry.key).append(" is overridden by ").append(entry.source).append('\n');
            builder.append(entry.key).append('=').append(entry.maskedValue()).append('\n');
        }
        return builder.toString();
    }
}
