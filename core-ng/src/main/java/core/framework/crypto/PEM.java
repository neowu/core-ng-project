package core.framework.crypto;


import core.framework.util.Encodings;

import java.util.stream.Collectors;

/**
 * @author neo
 */
public final class PEM {
    public static String toPEM(String type, byte[] content) {
        var builder = new StringBuilder("-----BEGIN ")
                .append(type)
                .append("-----");
        String encodedContent = Encodings.base64(content);
        int length = encodedContent.length();
        for (int i = 0; i < length; i++) {
            if (i % 64 == 0) builder.append('\n');
            builder.append(encodedContent.charAt(i));
        }
        builder.append("\n-----END ").append(type).append("-----");
        return builder.toString();
    }

    public static byte[] fromPEM(String pem) {
        String content = pem.lines().filter(line -> !line.startsWith("-----"))
                            .collect(Collectors.joining());
        return Encodings.decodeBase64(content);
    }
}
