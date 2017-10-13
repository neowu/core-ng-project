package core.framework.util;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author neo
 */
public final class Randoms {
    private static final String ALPHA_NUMERIC = "0123456789abcdefghijklmnopqrstuvwxyz";

    public static String alphaNumeric(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++)
            builder.append(ALPHA_NUMERIC.charAt(ThreadLocalRandom.current().nextInt(ALPHA_NUMERIC.length())));
        return builder.toString();
    }

    public static double number(double min, double max) {
        return (max - min) * ThreadLocalRandom.current().nextDouble() + min;
    }
}
