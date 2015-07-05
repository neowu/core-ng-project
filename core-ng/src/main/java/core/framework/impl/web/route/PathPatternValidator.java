package core.framework.impl.web.route;

import core.framework.api.util.Exceptions;
import core.framework.api.util.Sets;
import core.framework.api.util.Strings;

import java.util.Set;

/**
 * @author neo
 */
class PathPatternValidator {
    void validate(String pathPattern) {
        if (Strings.empty(pathPattern))
            throw Exceptions.error("path pattern must not be empty, pathPattern={}", pathPattern);

        if (!pathPattern.startsWith("/"))
            throw Exceptions.error("path pattern must start with \"/\", pathPattern={}", pathPattern);

        if (pathPattern.contains("./") || pathPattern.endsWith("."))
            throw Exceptions.error("path pattern must not contain \"/./\", \"/../\" or end with \".\", pathPattern={}", pathPattern);

        // to make sure anyone to use /{name} as dynamic path by mistake
        if (pathPattern.contains("{") || pathPattern.contains("}"))
            throw Exceptions.error("path pattern must not contain \"{\", \"}\", pathPattern={}", pathPattern);

        Set<String> variables = Sets.newHashSet();
        String[] tokens = pathPattern.split("/");
        for (String token : tokens) {
            if (token.startsWith(":")) {
                int paramIndex = token.indexOf('(');
                int endIndex = paramIndex > 0 ? paramIndex : token.length();
                boolean notDuplicated = variables.add(token.substring(1, endIndex));
                if (!notDuplicated)
                    throw Exceptions.error("path must not have duplicated param name, path={}", pathPattern);
            }
        }
    }
}
