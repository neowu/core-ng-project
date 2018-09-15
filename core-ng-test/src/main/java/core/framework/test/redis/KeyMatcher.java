package core.framework.test.redis;

/**
 * @author neo
 */
class KeyMatcher {
    private final String pattern;

    KeyMatcher(String pattern) {
        this.pattern = pattern;
    }

    // only support '*' and '?', refer to https://redis.io/commands/keys
    boolean matches(String key) {
        int keyLength = key.length();
        int patternLength = pattern.length();
        boolean state[][] = new boolean[keyLength + 1][patternLength + 1];
        state[0][0] = true;     // empty key matches empty pattern
        for (int i = 0; i < patternLength; i++) {
            if (pattern.charAt(i) == '*') state[0][i + 1] = state[0][i];    // whether first empty matches pattern
        }
        for (int i = 0; i < keyLength; i++) {
            for (int j = 0; j < patternLength; j++) {
                char ch = pattern.charAt(j);
                if (ch == '?' || key.charAt(i) == ch) {
                    state[i + 1][j + 1] = state[i][j];
                } else if (ch == '*') {
                    state[i + 1][j + 1] = state[i][j + 1] || state[i + 1][j];
                }
            }
        }
        return state[keyLength][patternLength];
    }
}
