package core.framework.test;

import core.framework.api.util.Lists;
import org.junit.Assert;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Ensure integrity of all the environment configuration files/structure
 *
 * @author neo
 * @version 1.2
 */
public final class EnvConfValidator {
    private static final String ENV_ROOT_DIR = "conf";
    private static final String RESOURCE_DIR = "resources";
    private static final String MAIN_RESOURCE_DIR = "src/main/resources";
    private static final String TEST_RESOURCE_DIR = "src/test/resources";

    private final List<String> envResourceDirs = Lists.newArrayList();

    public EnvConfValidator() {
        File envRootDir = new File(ENV_ROOT_DIR);
        Assert.assertTrue("env root directory does not exists, please check specified dir: " + envRootDir.getAbsolutePath(), envRootDir.exists());
        Assert.assertTrue("env root directory is not directory, please check: " + envRootDir.getAbsolutePath(), envRootDir.isDirectory());

        File[] envDirs = envRootDir.listFiles(File::isDirectory);
        for (File dir : envDirs) {
            envResourceDirs.add(String.format("%s/%s/%s", ENV_ROOT_DIR, dir.getName(), RESOURCE_DIR));
        }
    }

    public void validate() {
        validateEnvConfigFilesOverrideDefault();
        validateEnvPropertyFilesOverrideDefault();
        validateTestPropertyFilesOverridesDefault();
    }

    // all Files under /conf/{env} must have corresponding files under src/main/java/resources
    void validateEnvConfigFilesOverrideDefault() {
        boolean foundError = false;
        StringBuilder errorMessage = new StringBuilder("unnecessary resources found:\n");
        for (String envResourceDir : envResourceDirs) {
            EnvironmentResourcesValidator validator = new EnvironmentResourcesValidator(envResourceDir);
            if (validator.hasUnnecessaryResources()) {
                foundError = true;
                List<String> unnecessaryResources = validator.getUnnecessaryResources();
                errorMessage.append(createUnnecessaryResourcesErrorMessage(unnecessaryResources, envResourceDir));
            }
        }

        Assert.assertFalse(errorMessage.toString(), foundError);
    }

    void validateEnvPropertyFilesOverrideDefault() {
        List<String> inconsistentPropertyFiles = new ArrayList<>();
        for (String environmentDirectoryPath : envResourceDirs) {
            EnvPropertiesValidator envPropertiesValidator = new EnvPropertiesValidator(environmentDirectoryPath);
            List<String> envInconsistentPropertyFiles = envPropertiesValidator.compareToMainProperties();
            inconsistentPropertyFiles.addAll(envInconsistentPropertyFiles);
        }

        Assert.assertTrue(buildInconsistentPropertiesErrorMessage(inconsistentPropertyFiles), inconsistentPropertyFiles.isEmpty());
    }

    void validateTestPropertyFilesOverridesDefault() {
        List<String> inconsistentPropertyFiles = new ArrayList<>();
        EnvPropertiesValidator envPropertiesValidator = new EnvPropertiesValidator(TEST_RESOURCE_DIR);
        List<String> envInconsistentPropertyFiles = envPropertiesValidator.compareToMainProperties();
        inconsistentPropertyFiles.addAll(envInconsistentPropertyFiles);
        Assert.assertTrue(buildInconsistentPropertiesErrorMessage(inconsistentPropertyFiles), inconsistentPropertyFiles.isEmpty());
    }

    private String createUnnecessaryResourcesErrorMessage(List<String> environmentResources, String env) {
        StringBuilder builder = new StringBuilder();
        for (String resource : environmentResources) {
            builder.append('\t');
            builder.append(env).append("/" + RESOURCE_DIR + "/").append(resource);
            builder.append('\n');
        }
        return builder.toString();
    }

    private String buildInconsistentPropertiesErrorMessage(List<String> inconsistentPropertyFiles) {
        StringBuilder builder = new StringBuilder("inconsistent property files found:\n");
        for (String resource : inconsistentPropertyFiles) {
            builder.append('\t');
            builder.append(resource);
            builder.append('\n');
        }
        return builder.toString();
    }

    static class EnvironmentResourcesValidator {
        private final List<String> environmentResources;
        private final List<String> mainResources;

        public EnvironmentResourcesValidator(String environmentResourcesDirectory) {
            environmentResources = new ResourceFolderScanner(environmentResourcesDirectory).getFiles();
            mainResources = new ResourceFolderScanner(MAIN_RESOURCE_DIR).getFiles();
        }

        public boolean hasUnnecessaryResources() {
            return !getUnnecessaryResources(mainResources, environmentResources).isEmpty();
        }

        public List<String> getUnnecessaryResources() {
            return getUnnecessaryResources(mainResources, environmentResources);
        }

        private List<String> getUnnecessaryResources(List<String> mainResources, List<String> environmentResources) {
            List<String> tempEnvironmentResources = new ArrayList<>(environmentResources);
            tempEnvironmentResources.removeAll(mainResources);
            return tempEnvironmentResources;
        }
    }

    static class EnvPropertiesValidator {
        private final String environment;
        private final List<String> environmentPropertyFiles;

        public EnvPropertiesValidator(String environment) {
            this.environment = environment;
            environmentPropertyFiles = new ResourceFolderScanner(environment).getPropertyFiles();
        }

        public List<String> compareToMainProperties() {
            List<String> inconsistentPropertyFiles = new ArrayList<>();
            for (String environmentPropertyFile : environmentPropertyFiles) {
                String environmentPropertyPath = environment + "/" + environmentPropertyFile;
                Properties environmentProperties = loadPropertiesFromFile(environmentPropertyPath);

                File propsFile = new File(MAIN_RESOURCE_DIR + "/" + environmentPropertyFile);
                if (!propsFile.exists()) {
                    continue;
                }
                Properties mainProperties = loadPropertiesFromFile(propsFile.getPath());

                if (propertiesNotEqual(environmentProperties, mainProperties)) {
                    inconsistentPropertyFiles.add(environmentPropertyPath);
                }
            }

            return inconsistentPropertyFiles;
        }

        private boolean propertiesNotEqual(Properties properties1, Properties properties2) {
            ArrayList<Object> environmentPropertiesList = Collections.list(properties1.keys());
            ArrayList<Object> mainPropertiesList = Collections.list(properties2.keys());

            ArrayList<Object> environmentPropertiesList2 = Collections.list(properties1.keys());
            environmentPropertiesList.removeAll(mainPropertiesList);
            mainPropertiesList.removeAll(environmentPropertiesList2);

            return !environmentPropertiesList.isEmpty() || !mainPropertiesList.isEmpty();
        }

        private Properties loadPropertiesFromFile(String propertiesPath) {
            try (InputStream stream = new BufferedInputStream(new FileInputStream(propertiesPath))) {
                Properties properties = new Properties();
                properties.load(stream);
                return properties;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    static class ResourceFolderScanner {
        final String path;

        public ResourceFolderScanner(String path) {
            this.path = path;
        }

        public List<String> getFiles() {
            List<String> results = new ArrayList<>();
            getFilesRecursively(path, results, null);
            return removePathPrefix(results);
        }

        public List<String> getPropertyFiles() {
            List<String> results = new ArrayList<>();
            FileFilter filter = file -> file.isDirectory() || file.getName().endsWith(".properties");
            getFilesRecursively(path, results, filter);

            return removePathPrefix(results);
        }

        private void getFilesRecursively(String directoryPath, List<String> result, FileFilter filter) {
            File dir = new File(directoryPath);
            if (!dir.exists()) return;
            File[] listFiles = dir.listFiles(filter);
            for (File file : listFiles) {
                if (file.isDirectory()) {
                    getFilesRecursively(directoryPath + "/" + file.getName(), result, filter);
                }
                if (file.isFile()) {
                    result.add(directoryPath + "/" + file.getName());
                }
            }
        }

        private List<String> removePathPrefix(List<String> stringList) {
            List<String> result = new ArrayList<>();
            for (String string : stringList) {
                result.add(string.substring(path.length() + 1, string.length()));
            }
            return result;
        }
    }
}
