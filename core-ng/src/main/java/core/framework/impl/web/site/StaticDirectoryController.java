package core.framework.impl.web.site;

import core.framework.api.http.ContentType;
import core.framework.api.web.Controller;
import core.framework.api.web.Request;
import core.framework.api.web.Response;
import core.framework.api.web.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

/**
 * @author neo
 */
public final class StaticDirectoryController implements Controller {
    private final Logger logger = LoggerFactory.getLogger(StaticDirectoryController.class);
    private final Path contentDirectory;

    public StaticDirectoryController(Path contentDirectory) {
        this.contentDirectory = contentDirectory;
    }

    @Override
    public Response execute(Request request) throws Exception {
        String path = request.pathParam("path");
        Path filePath = contentDirectory.resolve(path);
        logger.debug("requestFile={}", filePath);

        if (!Files.isRegularFile(filePath, LinkOption.NOFOLLOW_LINKS) || !filePath.startsWith(contentDirectory))
            throw new NotFoundException("not found, path=" + request.path());

        Response response = Response.file(filePath);
        ContentType contentType = MimeTypes.get(filePath.getFileName().toString());
        if (contentType != null) response.contentType(contentType);
        return response;
    }
}
