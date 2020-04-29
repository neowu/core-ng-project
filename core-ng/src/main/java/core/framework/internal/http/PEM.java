package core.framework.internal.http;

import core.framework.util.Encodings;

import java.util.stream.Collectors;

/**
 * @author neo
 */
public class PEM {
    public static byte[] decode(String pem) {
        String content = pem.lines().filter(line -> !line.startsWith("-----"))
                            .collect(Collectors.joining());
        return Encodings.decodeBase64(content);
    }
}
