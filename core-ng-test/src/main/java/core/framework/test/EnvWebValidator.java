package core.framework.test;

import core.framework.api.web.site.WebDirectory;
import org.junit.Assert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author neo
 */
public final class EnvWebValidator {
    public void validate() throws IOException {
        WebDirectory webDirectory = new WebDirectory();
        Path conf = Paths.get("conf");
        Assert.assertTrue("conf must be directory, conf=" + conf.toAbsolutePath(), Files.isDirectory(conf));

        List<Path> webDirs = Files.list(conf)
                                  .filter(Files::isDirectory)
                                  .map(path -> path.resolve("web"))
                                  .filter(Files::exists)
                                  .collect(Collectors.toList());

        for (Path webDir : webDirs) {
            Assert.assertTrue("conf/env/web must be directory, path=" + webDir, Files.isDirectory(webDir));
            assertOverridesDefault(webDir, webDirectory.root());
        }
    }

    private void assertOverridesDefault(Path webDir, Path defaultWebDir) throws IOException {
        Files.walk(webDir).forEach(path -> {
            Path defaultFile = defaultWebDir.resolve(webDir.relativize(path));
            Assert.assertTrue("conf/env/web must override default web dir, path=" + path + ", defaultWebDir=" + defaultWebDir, Files.exists(defaultFile));
        });
    }
}
