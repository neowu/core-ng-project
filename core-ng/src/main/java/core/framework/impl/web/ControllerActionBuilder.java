package core.framework.impl.web;

import core.framework.http.HTTPMethod;
import core.framework.util.ASCII;
import core.framework.util.Strings;

/**
 * @author neo
 */
public class ControllerActionBuilder {
    private final HTTPMethod method;
    private final String pathPattern;

    public ControllerActionBuilder(HTTPMethod method, String pathPattern) {
        this.method = method;
        this.pathPattern = pathPattern;
    }

    public String build() {
        return "web/" + ASCII.toLowerCase(method.name()) + "-" + transformPathPattern();
    }

    private String transformPathPattern() {
        if ("/".equals(pathPattern)) return "root";

        String[] tokens = Strings.split(pathPattern, '/');
        StringBuilder builder = new StringBuilder(pathPattern.length());
        int index = 0;
        for (String token : tokens) {
            if (token.length() == 0) continue;
            if (index > 0) builder.append('-');
            if (Strings.startsWith(token, ':')) {
                int paramIndex = token.indexOf('(');
                int endIndex = paramIndex > 0 ? paramIndex : token.length();
                builder.append(token.substring(1, endIndex));
            } else {
                builder.append(token);
            }
            index++;
        }
        return builder.toString();
    }
}
