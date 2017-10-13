package core.framework.util;

import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Path;

/**
 * @author neo
 */
public class FilesTest {
    @Test
    public void tempFile() {
        Path path = Files.tempFile();
        Assert.assertTrue(path.toString().endsWith(".tmp"));
    }
}
