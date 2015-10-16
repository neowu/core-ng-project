package core.framework.test;

import org.junit.Assert;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * @author neo
 */
public final class EnvResourceValidator {
    public void validate() throws IOException {
        Path conf = Paths.get("conf");
        Assert.assertTrue("conf must be directory, conf=" + conf.toAbsolutePath(), Files.isDirectory(conf));

        List<Path> resourceDirs = Files.list(conf)
            .filter(Files::isDirectory)
            .map(path -> path.resolve("resources"))
            .filter(Files::exists)
            .collect(Collectors.toList());

        for (Path resourceDir : resourceDirs) {
            Assert.assertTrue("conf/env/resources must be directory, path=" + resourceDir, Files.isDirectory(resourceDir));
            assertOverridesDefault(resourceDir);
            assertPropertyOverridesDefault(resourceDir);
        }

        Path testResourceDir = Paths.get("src/test/resources");
        if (Files.exists(testResourceDir))
            assertPropertyOverridesDefault(testResourceDir);
    }

    private void assertOverridesDefault(Path resourceDir) throws IOException {
        Path defaultResourceDir = Paths.get("src/main/resources");
        Files.walk(resourceDir).forEach(path -> {
            Path defaultFile = defaultResourceDir.resolve(resourceDir.relativize(path));
            Assert.assertTrue("conf/env/resources must override src/main/resources, path=" + path, Files.exists(defaultFile));
        });
    }

    private void assertPropertyOverridesDefault(Path resourceDir) throws IOException {
        Path defaultResourceDir = Paths.get("src/main/resources");
        List<Path> propertyFiles = Files.walk(resourceDir)
            .filter(path -> Files.isDirectory(path) || path.toString().endsWith(".properties"))
            .collect(Collectors.toList())
            .stream().filter(Files::isRegularFile)
            .collect(Collectors.toList());
        for (Path propertyFile : propertyFiles) {
            Path defaultPropertyFile = defaultResourceDir.resolve(resourceDir.relativize(propertyFile));
            // for src/test/resources, ignore non override property file
            if (!Files.exists(defaultPropertyFile)) continue;
            assertPropertyFileMatches(propertyFile, defaultPropertyFile);
        }
    }

    private void assertPropertyFileMatches(Path envPropertyFile, Path defaultPropertyFile) throws IOException {
        Properties envProperties = loadProperties(envPropertyFile);
        Properties defaultProperties = loadProperties(defaultPropertyFile);
        Assert.assertEquals("property must override default, path=" + envPropertyFile, defaultProperties.keySet(), envProperties.keySet());
    }

    private Properties loadProperties(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path)) {
            Properties properties = new Properties();
            properties.load(reader);
            return properties;
        }
    }
}
