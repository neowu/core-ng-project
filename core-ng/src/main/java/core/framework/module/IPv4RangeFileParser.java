package core.framework.module;

import core.framework.util.ClasspathResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author neo
 */
public final class IPv4RangeFileParser {
    private final Logger logger = LoggerFactory.getLogger(IPv4RangeFileParser.class);
    private final String classpath;

    public IPv4RangeFileParser(String classpath) {
        this.classpath = classpath;
    }

    // write cidr for each line
    // comment line starts with '#'
    public List<String> parse() {
        logger.info("load ip range file, classpath={}", classpath);

        try (InputStream stream = ClasspathResources.stream(classpath);
             var reader = new BufferedReader(new InputStreamReader(stream, UTF_8))) {
            return reader.lines()
                .filter(line -> !line.isBlank() && line.charAt(0) != '#')
                .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
