package core.framework.api.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

/**
 * @author neo
 */
public final class ClasspathResources {
    public static String text(String path) {
        return new String(bytes(path), Charsets.UTF_8);
    }

    public static byte[] bytes(String path) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try (InputStream stream = loader.getResourceAsStream(path)) {
            if (stream == null) throw new Error("can not load resource, path=" + path);
            return InputStreams.bytes(stream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}

