package core.framework.impl.web.management;

import core.framework.api.util.Properties;
import core.framework.api.web.Controller;
import core.framework.api.web.Request;
import core.framework.api.web.Response;

import java.util.Set;
import java.util.TreeSet;

/**
 * @author neo
 */
public class PropertyController implements Controller {
    private final Properties properties;

    public PropertyController(Properties properties) {
        this.properties = properties;
    }

    @Override
    public Response execute(Request request) {
        ControllerHelper.validateFromLocalNetwork(request.clientIP());

        StringBuilder builder = new StringBuilder();
        Set<String> keys = new TreeSet<>(properties.keys());    // sort by key
        for (String key : keys) {
            builder.append(key);
            if (mask(key)) {
                builder.append("=(masked)\n");
            } else {
                builder.append('=').append(properties.get(key).orElse("")).append('\n');
            }
        }
        return Response.text(builder.toString());
    }

    private boolean mask(String key) {
        return key.contains("password") || key.contains("secret");
    }
}
