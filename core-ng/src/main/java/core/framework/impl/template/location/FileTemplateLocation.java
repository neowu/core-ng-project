package core.framework.impl.template.location;

import core.framework.api.util.Exceptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;

import static java.nio.file.Files.newBufferedReader;

/**
 * @author neo
 */
public final class FileTemplateLocation implements TemplateLocation {
    private final Path root;
    public final Path path;

    public FileTemplateLocation(Path root, String path) {
        this.root = root;
        if (!path.startsWith("/")) throw Exceptions.error("path must start with '/', path={}", path);
        this.path = root.resolve(path.substring(1));
    }

    @Override
    public BufferedReader reader() throws IOException {
        return newBufferedReader(path);
    }

    @Override
    public TemplateLocation location(String path) {
        return new FileTemplateLocation(root, path);
    }

    @Override
    public String toString() {
        return String.valueOf(path.toAbsolutePath());
    }
}
