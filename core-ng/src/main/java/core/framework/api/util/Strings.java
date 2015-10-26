package core.framework.api.util;

import org.slf4j.helpers.MessageFormatter;

/**
 * @author neo
 */
public final class Strings {
    public static byte[] bytes(String text) {
        return text.getBytes(Charsets.UTF_8);
    }

    public static String format(String pattern, Object... arguments) {
        return MessageFormatter.arrayFormat(pattern, arguments).getMessage();
    }

    public static int compare(String text1, String text2) {
        if (text1 == null && text2 == null)
            return 0;
        if (text1 != null && text2 == null) {
            return 1;
        }
        if (text1 == null) {
            return -1;
        }
        return text1.compareTo(text2);
    }

    public static boolean isEmpty(String text) {
        if (text == null)
            return true;

        for (int i = 0; i < text.length(); i++) {
            if (!Character.isWhitespace(text.charAt(i)))
                return false;
        }
        return true;
    }

    public static boolean equals(String text1, String text2) {
        if (text1 == null)
            return text2 == null;

        return text1.equals(text2);
    }

    public static String truncate(String text, int maxLength) {
        if (text == null)
            return null;
        if (text.length() <= maxLength)
            return text;
        return text.substring(0, maxLength);
    }

    public static String trim(String text) {
        if (text == null)
            return null;
        return text.trim();
    }

    // only convert ascii chars, faster than JDK String.toUpperCase due to JDK needs to handle UTF and locale
    // impl refers to guava: https://github.com/google/guava/blob/master/guava/src/com/google/common/base/Ascii.java
    public static String toUpperCase(String text) {
        if (text == null) return null;
        int length = text.length();
        for (int i = 0; i < length; i++) {
            if (isLowerCase(text.charAt(i))) {
                char[] chars = text.toCharArray();
                for (int j = i; j < length; j++) {
                    char ch = chars[j];
                    if (isLowerCase(ch)) {
                        chars[j] = (char) (ch & 0x5f);
                    }
                }
                return String.valueOf(chars);
            }
        }
        return text;
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

    public static boolean isLowerCase(char ch) {
        return ch >= 'a' && ch <= 'z';
    }

    public static boolean isUpperCase(char ch) {
        return ch >= 'A' && ch <= 'Z';
    }
}
