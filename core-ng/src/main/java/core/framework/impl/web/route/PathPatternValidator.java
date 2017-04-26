package core.framework.impl.web.route;

import core.framework.api.util.ASCII;
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
        String[] tokens = Strings.split(pathPattern, '/');
        for (String token : tokens) {
            if (token.startsWith(":")) {
                validateVariable(token, pathPattern, variables);
            } else {
                validatePathSegment(token, pathPattern);
            }
        }
    }

    private void validateVariable(String token, String pathPattern, Set<String> variables) {
        int variablePatternIndex = token.indexOf('(');
        int endIndex = variablePatternIndex > 0 ? variablePatternIndex : token.length();

        String variable = token.substring(1, endIndex);
        for (int i = 0; i < variable.length(); i++) {
            char ch = variable.charAt(i);
            if (!ASCII.isLetter(ch)) throw Exceptions.error("path variable must be letter, variable={}, pathPattern={}", variable, pathPattern);
        }

        boolean isNew = variables.add(variable);
        if (!isNew) throw Exceptions.error("found duplicate param name, path={}", pathPattern);

        if (variablePatternIndex > 0) {
            String variablePattern = token.substring(variablePatternIndex);
            if (!"(*)".equals(variablePattern)) {
                throw Exceptions.error("path variable must be :name or :name(*), variable={}, pathPattern={}", token, pathPattern);
            }
        }
    }

    private void validatePathSegment(String segment, String pathPattern) {
        if (segment.length() == 0) return;

        if (segment.charAt(segment.length() - 1) == '.')
            throw Exceptions.error("path segment must not end with '.', segment={}, pathPattern={}", segment, pathPattern);

        for (int i = 0; i < segment.length(); i++) {
            char ch = segment.charAt(i);
            if (!ASCII.isLetter(ch) && !ASCII.isDigit(ch) && ch != '_' && ch != '-' && ch != '.') {
                throw Exceptions.error("path segment must only contain (letter / digit / _ / - / .), segment={}, pathPattern={}", segment, pathPattern);
            }
        }
    }
}
