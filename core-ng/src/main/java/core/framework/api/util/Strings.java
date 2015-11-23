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
}
