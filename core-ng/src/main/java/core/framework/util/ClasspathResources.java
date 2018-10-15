package core.framework.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author neo
 */
public final class ClasspathResources {
    public static String text(String path) {
        return new String(bytes(path), UTF_8);
    }

    public static byte[] bytes(String path) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream stream = loader.getResourceAsStream(path);
        if (stream == null) throw new Error("can not load resource, path=" + path);
        try (stream) {
            return stream.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}

