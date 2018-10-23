package core.framework.util;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class FilesTest {
    @Test
    void tempFile() throws IOException {
        Path file = Files.tempFile();
        assertThat(file.getFileName().toString()).endsWith(".tmp");

        java.nio.file.Files.write(file, Strings.bytes("test"));
        assertThat(Files.size(file)).isEqualTo(4);
        assertThat(Files.lastModified(file)).isNotNull();

        Files.delete(file);
    }

    @Test
    void tempDir() {
        Path tempDir = Files.tempDir();
        assertThat(tempDir).exists().isDirectory();

        Files.deleteDir(tempDir);
    }
}
