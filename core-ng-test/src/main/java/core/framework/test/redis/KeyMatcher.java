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
        boolean state[][] = new boolean[key.length() + 1][pattern.length() + 1];
        state[0][0] = true;     // empty key matches empty pattern
        for (int i = 0; i < pattern.length(); i++) {
            if (pattern.charAt(i) == '*') state[0][i + 1] = state[0][i];    // whether first empty matches pattern
        }
        for (int i = 0; i < key.length(); i++) {
            for (int j = 0; j < pattern.length(); j++) {
                if (key.charAt(i) == pattern.charAt(j) || pattern.charAt(j) == '?') {
                    state[i + 1][j + 1] = state[i][j];
                } else if (pattern.charAt(j) == '*') {
                    state[i + 1][j + 1] = state[i][j + 1] || state[i + 1][j];
                }
            }
        }
        return state[key.length()][pattern.length()];
    }
}
