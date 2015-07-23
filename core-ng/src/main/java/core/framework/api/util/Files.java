package core.framework.api.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.UUID;

import static java.nio.file.Files.copy;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createDirectory;
import static java.nio.file.Files.delete;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.getLastModifiedTime;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.walkFileTree;

/**
 * @author neo
 */
public final class Files {
    private static final Logger LOGGER = LoggerFactory.getLogger(Files.class);

    public static String text(Path path) {
        return new String(bytes(path), Charsets.UTF_8);
    }

    public static byte[] bytes(Path path) {
        StopWatch watch = new StopWatch();
        try {
            return readAllBytes(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            LOGGER.debug("bytes, path={}, elapsedTime={}", path, watch.elapsedTime());
        }
    }

    public static void copyDirectory(Path source, Path destination) {
        StopWatch watch = new StopWatch();
        try {
            walkFileTree(source, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attributes) throws IOException {
                    Path targetPath = destination.resolve(source.relativize(dir));
                    if (!exists(targetPath)) createDirectory(targetPath);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                    copy(file, destination.resolve(source.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            LOGGER.debug("copy directory, source={}, destination={}, elapsedTime={}", source, destination, watch.elapsedTime());
        }
    }

    public static void deleteDirectory(Path directory) {
        StopWatch watch = new StopWatch();
        try {
            walkFileTree(directory, new SimpleFileVisitor<Path>() {
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
            LOGGER.debug("delete directory, path={}, elapsedTime={}", directory, watch.elapsedTime());
        }
    }

    public static Path tempFile() {
        String tempDir = System.getProperty("java.io.tmpdir");
        Path path = Paths.get(tempDir + "/" + UUID.randomUUID().toString() + ".tmp");
        LOGGER.debug("create temp file path, path={}", path);
        return path;
    }

    public static Path tempDirectory() {
        String tempDir = System.getProperty("java.io.tmpdir");
        Path path = Paths.get(tempDir + "/" + UUID.randomUUID().toString());
        try {
            createDirectories(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        LOGGER.debug("create temp directory, path={}", path);
        return path;
    }

    public static Instant lastModified(Path path) {
        try {
            return getLastModifiedTime(path).toInstant();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
