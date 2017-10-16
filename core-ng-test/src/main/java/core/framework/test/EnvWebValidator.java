package core.framework.test;

import core.framework.util.Strings;
import core.framework.web.site.WebDirectory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author neo
 */
public final class EnvWebValidator {
    public void validate() throws IOException {
        WebDirectory webDirectory = new WebDirectory();
        Path conf = Paths.get("conf");
        assertTrue(Files.isDirectory(conf), "conf must be directory, conf=" + conf.toAbsolutePath());

        List<Path> webDirs = Files.list(conf)
                                  .filter(Files::isDirectory)
                                  .map(path -> path.resolve("web"))
                                  .filter(Files::exists)
                                  .collect(Collectors.toList());

        for (Path webDir : webDirs) {
            assertTrue(Files.isDirectory(webDir), "conf/env/web must be directory, path=" + webDir);
            assertOverridesDefault(webDir, webDirectory.root());
        }
    }

    private void assertOverridesDefault(Path webDir, Path defaultWebDir) throws IOException {
        Files.walk(webDir).forEach(path -> {
            Path defaultFile = defaultWebDir.resolve(webDir.relativize(path));
            assertTrue(Files.exists(defaultFile), Strings.format("conf/env/web must override default web dir, path={}, defaultWebDir={}", path, defaultWebDir));
        });
    }
}
