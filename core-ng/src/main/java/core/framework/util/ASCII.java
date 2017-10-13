package core.framework.util;

/**
 * @author neo
 */
public final class ASCII {
    // only convert ascii chars, faster than JDK String.toUpperCase due to JDK needs to handle UTF and locale
    // refers to https://github.com/google/guava/blob/master/guava/src/com/google/common/base/Ascii.java
    public static String toUpperCase(String text) {
        if (text == null) return null;
        int length = text.length();
        for (int i = 0; i < length; i++) {
            if (isLowerCase(text.charAt(i))) {
                char[] chars = text.toCharArray();
                for (int j = i; j < length; j++) {
                    char ch = chars[j];
                    if (isLowerCase(ch)) {
                        chars[j] = (char) (ch & 0x5F);
                    }
                }
                return String.valueOf(chars);
            }
        }
        return text;
    }

    public static char toUpperCase(char ch) {
        if (isLowerCase(ch)) return (char) (ch & 0x5F);
        return ch;
    }

    public static String toLowerCase(String text) {
        if (text == null) return null;
        int length = text.length();
        for (int i = 0; i < length; i++) {
            if (isUpperCase(text.charAt(i))) {
                char[] chars = text.toCharArray();
                for (int j = i; j < length; j++) {
                    char ch = chars[j];
                    if (isUpperCase(ch)) {
                        chars[j] = (char) (ch ^ 0x20);
                    }
                }
                return String.valueOf(chars);
            }
        }
        return text;
    }

    public static char toLowerCase(char ch) {
        if (isUpperCase(ch)) return (char) (ch ^ 0x20);
        return ch;
    }

    public static boolean isLowerCase(char ch) {
        return ch >= 'a' && ch <= 'z';
    }

    public static boolean isUpperCase(char ch) {
        return ch >= 'A' && ch <= 'Z';
    }

    public static boolean isDigit(char ch) {
        return ch >= '0' && ch <= '9';
    }

    public static boolean isLetter(char ch) {
        return isLowerCase(ch) || isUpperCase(ch);
    }
}
