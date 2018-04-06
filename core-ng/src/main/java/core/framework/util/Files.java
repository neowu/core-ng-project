package core.framework.util;

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
import static java.nio.file.Files.delete;
import static java.nio.file.Files.getLastModifiedTime;
import static java.nio.file.Files.move;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.walkFileTree;

/**
 * @author neo
 */
public final class Files {
    private static final Logger LOGGER = LoggerFactory.getLogger(Files.class);

    public static String text(Path file) {
        return new String(bytes(file), Charsets.UTF_8);
    }

    public static byte[] bytes(Path file) {
        StopWatch watch = new StopWatch();
        try {
            return readAllBytes(file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            LOGGER.debug("bytes, file={}, elapsedTime={}", file, watch.elapsedTime());
        }
    }

    public static void createDir(Path directory) {
        try {
            createDirectories(directory);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void copyDir(Path source, Path destination) {
        StopWatch watch = new StopWatch();
        try {
            walkFileTree(source, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attributes) throws IOException {
                    Path targetPath = destination.resolve(source.relativize(dir));
                    if (!exists(targetPath)) createDirectories(targetPath);
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
            LOGGER.debug("copyDir, source={}, destination={}, elapsedTime={}", source, destination, watch.elapsedTime());
        }
    }

    public static void deleteDir(Path directory) {
        StopWatch watch = new StopWatch();
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
            LOGGER.debug("deleteDir, directory={}, elapsedTime={}", directory, watch.elapsedTime());
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

    public static void deleteFile(Path file) {
        try {
            delete(file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void moveFile(Path source, Path destination) {
        try {
            move(source, destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean exists(Path file) {
        return java.nio.file.Files.exists(file);
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
