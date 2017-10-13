package core.framework.impl.web.site;

import core.framework.http.ContentType;
import core.framework.web.Controller;
import core.framework.web.Request;
import core.framework.web.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        Response response = Response.file(contentFile);
        if (contentType != null) response.contentType(contentType);
        return response;
    }
}
