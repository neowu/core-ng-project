package core.framework.web.site;

import core.framework.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author neo
 */
public final class WebDirectory {
    private final Logger logger = LoggerFactory.getLogger(WebDirectory.class);

    private final Path root;
    public boolean localEnv;

    public WebDirectory() {
        this.root = locateRootDirectory();
    }

    private Path locateRootDirectory() {
        String value = System.getProperty("core.webPath");
        if (value != null) {
            Path path = Paths.get(value).toAbsolutePath();
            if (Files.isDirectory(path)) {
                logger.info("found -Dcore.webPath as web directory, path={}", path);
                return path;
            }
        } else {
            Path path = findLocalRootDirectory();
            if (path != null) {
                logger.info("found local web directory, this should only happen in local dev env or test, path={}", path);
                localEnv = true;
                try {
                    return path.toRealPath();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }
        logger.info("can not locate web directory");
        return null;
    }

    private Path findLocalRootDirectory() {
        Path path = Paths.get("./src/main/dist/web");
        if (Files.isDirectory(path)) return path;
        return null;
    }

    public Path path(String path) {
        if (!Strings.startsWith(path, '/')) throw new Error("path must start with '/', path=" + path);
        return root().resolve(path.substring(1)).toAbsolutePath();
    }

    public Path root() {
        if (root == null)
            throw new Error("can not find web path, set working dir to module dir for local dev env, or check -Dcore.webPath for server env, workingDir=" + System.getProperty("user.dir"));
        return root;
    }
}
