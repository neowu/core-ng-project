package core.framework.crypto;


import core.framework.util.Encodings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;

/**
 * @author neo
 */
public final class PEM {
    public static String toPEM(String type, byte[] content) {
        StringBuilder builder = new StringBuilder("-----BEGIN ")
                .append(type)
                .append("-----");
        String encodedContent = Encodings.base64(content);
        for (int i = 0; i < encodedContent.length(); i++) {
            if (i % 64 == 0) builder.append('\n');
            builder.append(encodedContent.charAt(i));
        }
        builder.append("\n-----END ").append(type).append("-----");
        return builder.toString();
    }

    public static byte[] fromPEM(String pemContent) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new StringReader(pemContent))) {
            while (true) {
                String line = reader.readLine();
                if (line == null) break;
                if (line.startsWith("-----")) continue;
                content.append(line.replaceAll("\n", "").replaceAll("\r", ""));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return Encodings.decodeBase64(content.toString());
    }
}
