package core.framework.test.assertion;

import org.assertj.core.api.AbstractAssert;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
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
    private final Path mainResources;
    private final Path testResources;

    EnvResourceAssert(Path confPath, Path mainResources, Path testResources) {
        super(confPath, EnvResourceAssert.class);
        this.mainResources = mainResources;
        this.testResources = testResources;
    }

    public EnvResourceAssert() {
        this(Paths.get("conf").toAbsolutePath(),
            Paths.get("src/main/resources"),
            Paths.get("src/test/resources"));
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

            if (Files.exists(testResources)) {
                assertPropertyOverridesDefault(testResources);
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
        try (Stream<Path> stream = Files.walk(resourceDir)) {
            stream.forEach(path -> {
                Path defaultFile = mainResources.resolve(resourceDir.relativize(path));
                assertThat(defaultFile).as("%s must override src/main/resources", path).exists();
            });
        }
    }

    private void assertPropertyOverridesDefault(Path resourceDir) throws IOException {
        List<Path> propertyFiles;
        try (Stream<Path> stream = Files.walk(resourceDir).filter(path -> path.toString().endsWith(".properties"))) {
            propertyFiles = stream.collect(Collectors.toList());
        }
        for (Path propertyFile : propertyFiles) {
            Path defaultPropertyFile = mainResources.resolve(resourceDir.relativize(propertyFile));
            if (!Files.exists(defaultPropertyFile)) continue;   // for src/test/resources, ignore non override property file, conf/resources is checked by assertOverridesDefault

            Properties envProperties = loadProperties(propertyFile);
            Properties defaultProperties = loadProperties(defaultPropertyFile);
            assertThat(envProperties.keySet()).as("%s must override %s", propertyFile, defaultPropertyFile).isEqualTo(defaultProperties.keySet());
        }
    }

    private Properties loadProperties(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            var properties = new Properties();
            properties.load(reader);
            return properties;
        }
    }
}
