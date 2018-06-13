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

            List<Path> resourceDirs = Files.list(actual).filter(Files::isDirectory).map(path -> path.resolve("resources"))
                                           .filter(Files::exists)
                                           .collect(Collectors.toList());

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

    private void assertOverridesDefault(Path resourceDir) throws IOException {
        Path defaultResourceDir = Paths.get("src/main/resources");
        Files.walk(resourceDir).forEach(path -> {
            Path defaultFile = defaultResourceDir.resolve(resourceDir.relativize(path));
            assertThat(defaultFile).as("%s must override src/main/resources", defaultFile).exists();
        });
    }

    private void assertPropertyOverridesDefault(Path resourceDir) throws IOException {
        Path defaultResourceDir = Paths.get("src/main/resources");
        List<Path> propertyFiles = Files.walk(resourceDir).filter(path -> Files.isDirectory(path) || path.toString().endsWith(".properties"))
                                        .collect(Collectors.toList())
                                        .stream().filter(Files::isRegularFile)
                                        .collect(Collectors.toList());
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
