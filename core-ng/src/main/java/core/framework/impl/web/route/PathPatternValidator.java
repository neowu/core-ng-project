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
        if (Strings.isEmpty(pathPattern))
            throw Exceptions.error("path pattern must not be empty, pathPattern={}", pathPattern);

        if (!pathPattern.startsWith("/"))
            throw Exceptions.error("path pattern must start with '/', pathPattern={}", pathPattern);

        Set<String> variables = Sets.newHashSet();
        String[] tokens = pathPattern.split("/");
        for (String token : tokens) {
            if (token.startsWith(":")) {
                int paramIndex = token.indexOf('(');
                int endIndex = paramIndex > 0 ? paramIndex : token.length();
                String variable = token.substring(1, endIndex);
                validateVariable(variable, pathPattern);
                boolean notDuplicated = variables.add(variable);
                if (!notDuplicated)
                    throw Exceptions.error("path must not have duplicated param name, path={}", pathPattern);
            } else {
                validatePathSegment(token, pathPattern);
            }
        }
    }

    private void validatePathSegment(String segment, String pathPattern) {
        if (segment.length() == 0) return;

        if (segment.charAt(segment.length() - 1) == '.')
            throw Exceptions.error("path segment must not end with '.', segment={}, pathPattern={}", segment, pathPattern);

        for (int i = 0; i < segment.length(); i++) {
            char ch = segment.charAt(i);
            if (!isLetter(ch) && !isDigit(ch) && ch != '_' && ch != '-' && ch != '.') {
                throw Exceptions.error("path segment must only contain (letter / digit / _ / - / .), segment={}, pathPattern={}", segment, pathPattern);
            }
        }
    }

    private void validateVariable(String variable, String pathPattern) {
        for (int i = 0; i < variable.length(); i++) {
            char ch = variable.charAt(i);
            if (!isLetter(ch)) throw Exceptions.error("path variable must be letter, variable={}, pathPattern={}", variable, pathPattern);
        }
    }

    private boolean isLetter(char ch) {
        return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z');
    }

    private boolean isDigit(char ch) {
        return ch >= '0' && ch <= '9';
    }
}
