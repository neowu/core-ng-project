package core.framework.impl.web.route;

import core.framework.util.ASCII;
import core.framework.util.Exceptions;
import core.framework.util.Sets;
import core.framework.util.Strings;

import java.util.Set;

/**
 * @author neo
 */
public class PathPatternValidator {
    private final String pattern;

    public PathPatternValidator(String pattern) {
        this.pattern = pattern;
    }

    public void validate() {
        if (Strings.isEmpty(pattern))
            throw Exceptions.error("path pattern must not be empty, pattern={}", pattern);

        if (!Strings.startsWith(pattern, '/'))
            throw Exceptions.error("path pattern must start with '/', pattern={}", pattern);

        Set<String> variables = Sets.newHashSet();
        String[] tokens = Strings.split(pattern, '/');
        for (String token : tokens) {
            if (Strings.startsWith(token, ':')) {
                validateVariable(token, pattern, variables);
            } else {
                validatePathSegment(token, pattern);
            }
        }
    }

    private void validateVariable(String token, String pattern, Set<String> variables) {
        int variablePatternIndex = token.indexOf('(');
        int endIndex = variablePatternIndex > 0 ? variablePatternIndex : token.length();

        String variable = token.substring(1, endIndex);
        for (int i = 0; i < variable.length(); i++) {
            char ch = variable.charAt(i);
            if (!ASCII.isLetter(ch))
                throw Exceptions.error("path variable must be letter, variable={}, pattern={}", variable, pattern);
        }

        boolean isNew = variables.add(variable);
        if (!isNew) throw Exceptions.error("found duplicate param name, path={}", pattern);

        if (variablePatternIndex > 0) {
            String variablePattern = token.substring(variablePatternIndex);
            if (!"(*)".equals(variablePattern)) {
                throw Exceptions.error("path variable must be :name or :name(*), variable={}, pattern={}", token, pattern);
            }
        }
    }

    private void validatePathSegment(String segment, String pattern) {
        if (segment.length() == 0) return;

        if (segment.charAt(segment.length() - 1) == '.')
            throw Exceptions.error("path segment must not end with '.', segment={}, pattern={}", segment, pattern);

        for (int i = 0; i < segment.length(); i++) {
            char ch = segment.charAt(i);
            if (!ASCII.isLetter(ch) && !ASCII.isDigit(ch) && ch != '_' && ch != '-' && ch != '.') {
                throw Exceptions.error("path segment must only contain (letter / digit / _ / - / .), segment={}, pattern={}", segment, pattern);
            }
        }
    }
}
