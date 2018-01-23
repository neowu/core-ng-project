package core.framework.impl.web.site;

import core.framework.http.ContentType;
import core.framework.http.HTTPHeaders;
import core.framework.web.Request;
import core.framework.web.Response;
import core.framework.web.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.time.Duration;

/**
 * @author neo
 */
public final class StaticDirectoryController implements StaticContentController {
    private final Logger logger = LoggerFactory.getLogger(StaticDirectoryController.class);
    private final Path contentDirectory;
    String cacheHeader;

    public StaticDirectoryController(Path contentDirectory) {
        this.contentDirectory = contentDirectory;
    }

    @Override
    public Response execute(Request request) {
        String path = request.pathParam("path");
        Path filePath = contentDirectory.resolve(path);
        logger.debug("requestFile={}", filePath);

        if (!Files.isRegularFile(filePath, LinkOption.NOFOLLOW_LINKS) || !filePath.startsWith(contentDirectory))
            throw new NotFoundException("not found, path=" + request.path());

        Response response = Response.file(filePath);
        ContentType contentType = MimeTypes.get(filePath.getFileName().toString());
        if (contentType != null) response.contentType(contentType);
        if (cacheHeader != null) response.header(HTTPHeaders.CACHE_CONTROL, cacheHeader);
        return response;
    }

    @Override
    public void cache(Duration maxAge) {
        cacheHeader = "public, max-age=" + maxAge.getSeconds();
    }
}
