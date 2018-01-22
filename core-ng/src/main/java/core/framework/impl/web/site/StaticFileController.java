package core.framework.impl.web.site;

import core.framework.http.ContentType;
import core.framework.http.HTTPHeaders;
import core.framework.web.Controller;
import core.framework.web.Request;
import core.framework.web.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.Duration;

/**
 * @author neo
 */
public final class StaticFileController implements Controller {
    private final Logger logger = LoggerFactory.getLogger(StaticFileController.class);
    private final Path contentFile;
    private final ContentType contentType;
    private String cacheHeader;

    public StaticFileController(Path contentFile, Duration cacheMaxAge) {
        this.contentFile = contentFile;
        contentType = MimeTypes.get(contentFile.getFileName().toString());
        if (cacheMaxAge != null && cacheMaxAge.getSeconds() > 0) cacheHeader = "public, max-age=" + cacheMaxAge.getSeconds();
    }

    @Override
    public Response execute(Request request) {
        logger.debug("requestFile={}", contentFile);

        Response response = Response.file(contentFile);
        if (contentType != null) response.contentType(contentType);
        if (cacheHeader != null) response.header(HTTPHeaders.CACHE_CONTROL, cacheHeader);
        return response;
    }
}
