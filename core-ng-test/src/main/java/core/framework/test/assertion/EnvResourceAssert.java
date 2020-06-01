package core.framework.test.assertion;

import org.assertj.core.api.AbstractAssert;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
public class EnvResourceAssert extends AbstractAssert<EnvResourceAssert, Path> {
    public EnvResourceAssert() {
        super(Paths.get("conf").toAbsolutePath(), EnvResourceAssert.class);
    }

    public void overridesDefaultResources() {
        try {
            assertThat(actual).isDirectory();

            List<Path> resourceDirs = resourceDirs();

            for (Path resourceDir : resourceDirs) {
                assertThat(resourceDir).isDirectory();
                assertOverridesDefault(resourceDir);
                assertPropertyOverridesDefault(resourceDir);
            }

            Path testResourceDir = Paths.get("src/test/resources");
            if (Files.exists(testResourceDir)) {
                assertPropertyOverridesDefault(testResourceDir);
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private List<Path> resourceDirs() throws IOException {
        try (Stream<Path> stream = Files.list(actual)) {
            return stream.filter(Files::isDirectory)
                         .map(path -> path.resolve("resources")).filter(Files::exists)
                         .collect(Collectors.toList());
        }
    }

    private void assertOverridesDefault(Path resourceDir) throws IOException {
        Path defaultResourceDir = Paths.get("src/main/resources");
        try (Stream<Path> stream = Files.walk(resourceDir)) {
            stream.forEach(path -> {
                Path defaultFile = defaultResourceDir.resolve(resourceDir.relativize(path));
                assertThat(defaultFile).as("%s must override src/main/resources", path).exists();
            });
        }
    }

    private void assertPropertyOverridesDefault(Path resourceDir) throws IOException {
        Path defaultResourceDir = Paths.get("src/main/resources");
        List<Path> propertyFiles;
        try (Stream<Path> stream = Files.walk(resourceDir).filter(path -> path.toString().endsWith(".properties"))) {
            propertyFiles = stream.collect(Collectors.toList());
        }
        for (Path propertyFile : propertyFiles) {
            Path defaultPropertyFile = defaultResourceDir.resolve(resourceDir.relativize(propertyFile));
            if (!Files.exists(defaultPropertyFile)) continue;   // for src/test/resources, ignore non override property file, conf/resources is checked by assertOverridesDefault

            Properties envProperties = loadProperties(propertyFile);
            Properties defaultProperties = loadProperties(defaultPropertyFile);
            assertThat(envProperties.keySet()).as("%s must override %s", propertyFile, defaultPropertyFile).isEqualTo(defaultProperties.keySet());
        }
    }

    private Properties loadProperties(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path)) {
            Properties properties = new Properties();
            properties.load(reader);
            return properties;
        }
    }
}
