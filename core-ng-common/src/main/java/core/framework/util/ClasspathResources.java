package core.framework.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Iterator;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author neo
 */
public final class ClasspathResources {
    public static String text(String path) {
        return new String(bytes(path), UTF_8);
    }

    public static byte[] bytes(String path) {
        try (InputStream stream = stream(path)) {
            return stream.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static InputStream stream(String path) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            Iterator<URL> urls = loader.getResources(path).asIterator();
            return openStream(urls, path);
        } catch (IOException e) {
            throw new Error("can not load resource, path=" + path, e);
        }
    }

    static InputStream openStream(Iterator<URL> urls, String path) throws IOException {
        if (urls.hasNext()) {
            URL url = urls.next();
            // to avoid have same resource name within different jars, as the order of loading jar is not determined on different env/os, we'd better avoid potential unexpected behavior
            if (urls.hasNext()) throw new Error("found duplicate resources with same name, path=" + path);
            return url.openStream();
        }
        throw new Error("can not load resource, path=" + path);
    }
}

