package core.framework.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.UUID;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.delete;
import static java.nio.file.Files.getLastModifiedTime;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.walkFileTree;

/**
 * @author neo
 */
public final class Files {
    private static final Logger LOGGER = LoggerFactory.getLogger(Files.class);

    public static String text(Path file) {
        return new String(bytes(file), StandardCharsets.UTF_8);
    }

    public static byte[] bytes(Path file) {
        var watch = new StopWatch();
        try {
            return readAllBytes(file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            LOGGER.debug("bytes, file={}, elapsed={}", file, watch.elapsed());
        }
    }

    public static void createDir(Path directory) {
        try {
            createDirectories(directory);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void deleteDir(Path directory) {
        var watch = new StopWatch();
        try {
            walkFileTree(directory, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                    delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
                    delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            LOGGER.debug("deleteDir, directory={}, elapsed={}", directory, watch.elapsed());
        }
    }

    public static Path tempFile() {
        String tempDir = System.getProperty("java.io.tmpdir");
        return Paths.get(tempDir + "/" + UUID.randomUUID().toString() + ".tmp");
    }

    public static Path tempDir() {
        String tempDir = System.getProperty("java.io.tmpdir");
        Path path = Paths.get(tempDir + "/" + UUID.randomUUID().toString());
        createDir(path);
        return path;
    }

    public static long size(Path file) {
        try {
            return java.nio.file.Files.size(file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Instant lastModified(Path file) {
        try {
            return getLastModifiedTime(file).toInstant();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
