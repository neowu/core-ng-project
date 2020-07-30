package core.framework.internal.web.route;

import core.framework.util.ASCII;
import core.framework.util.Sets;
import core.framework.util.Strings;

import java.util.Set;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public class PathPatternValidator {
    private final String pattern;
    private final boolean allowWildcard;

    public PathPatternValidator(String pattern, boolean allowWildcard) {
        this.pattern = pattern;
        this.allowWildcard = allowWildcard;
    }

    public void validate() {
        if (Strings.isBlank(pattern)) throw new Error("path pattern must not be blank, pattern=" + pattern);

        if (!Strings.startsWith(pattern, '/')) throw new Error("path pattern must start with '/', pattern=" + pattern);

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
        int patternIndex = token.indexOf('(');
        int endIndex = patternIndex > 0 ? patternIndex : token.length();

        String variable = token.substring(1, endIndex);
        int length = variable.length();
        for (int i = 0; i < length; i++) {
            char ch = variable.charAt(i);
            if (!ASCII.isLetter(ch))
                throw new Error(format("path variable must be letter, variable={}, pattern={}", variable, pattern));
        }

        boolean isNew = variables.add(variable);
        if (!isNew) throw new Error("found duplicate param name, path=" + pattern);

        if (patternIndex > 0) {
            String variablePattern = token.substring(patternIndex);
            if (!"(*)".equals(variablePattern))
                throw new Error(format("path variable must be :name or :name(*), variable={}, pattern={}", token, pattern));
            if (!allowWildcard)
                throw new Error(format("wildcard path variable is not allowed, variable={}, pattern={}", token, pattern));
        }
    }

    private void validatePathSegment(String segment, String pattern) {
        int length = segment.length();
        if (length == 0) return;

        if (segment.charAt(length - 1) == '.')
            throw new Error(format("path segment must not end with '.', segment={}, pattern={}", segment, pattern));

        for (int i = 0; i < length; i++) {
            char ch = segment.charAt(i);
            if (ch != '_' && ch != '-' && ch != '.' && !ASCII.isLetter(ch) && !ASCII.isDigit(ch)) {
                throw new Error(format("path segment must only contain (letter/digit/_/-/.), segment={}, pattern={}", segment, pattern));
            }
        }
    }
}
