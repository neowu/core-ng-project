package core.framework.util;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author neo
 */
class FilesTest {
    @Test
    void tempFile() {
        Path path = Files.tempFile();
        assertTrue(path.toString().endsWith(".tmp"));
    }
}
