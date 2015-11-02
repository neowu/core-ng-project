package core.framework.impl.web.site;

import core.framework.api.http.ContentType;
import core.framework.api.web.Controller;
import core.framework.api.web.Request;
import core.framework.api.web.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;

/**
 * @author neo
 */
public final class StaticFileController implements Controller {
    private final Logger logger = LoggerFactory.getLogger(StaticFileController.class);
    private final Path contentFile;
    private final ContentType contentType;

    public StaticFileController(Path contentFile) {
        this.contentFile = contentFile;
        contentType = MimeTypes.get(contentFile.getFileName().toString());
    }

    @Override
    public Response execute(Request request) throws Exception {
        logger.debug("requestFile={}", contentFile);

        File file = contentFile.toFile();
        Response response = Response.file(file);
        if (contentType != null) response.contentType(contentType);
        return response;
    }
}
