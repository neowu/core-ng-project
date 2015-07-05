package core.framework.api.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.UUID;

import static java.nio.file.Files.copy;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createDirectory;
import static java.nio.file.Files.delete;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.walkFileTree;

/**
 * @author neo
 */
public final class Files {
    public static String text(Path path) {
        return new String(bytes(path), Charsets.UTF_8);
    }

    public static byte[] bytes(Path path) {
        try {
            return readAllBytes(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void copyDirectory(Path source, Path destination) {
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
        }
    }

    public static void deleteDirectory(Path directory) {
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
        }
    }

    public static Path tempDirectory() {
        String tempDir = System.getProperty("java.io.tmpdir");
        Path path = Paths.get(tempDir + "/" + UUID.randomUUID().toString());
        try {
            createDirectories(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return path;
    }
}
