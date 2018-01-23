package core.framework.impl.web.site;

import core.framework.http.ContentType;
import core.framework.http.HTTPHeaders;
import core.framework.web.Request;
import core.framework.web.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.Duration;

/**
 * @author neo
 */
public final class StaticFileController implements StaticContentController {
    private final Logger logger = LoggerFactory.getLogger(StaticFileController.class);
    private final Path contentFile;
    private final ContentType contentType;
    private String cacheHeader;

    public StaticFileController(Path contentFile) {
        this.contentFile = contentFile;
        contentType = MimeTypes.get(contentFile.getFileName().toString());
    }

    @Override
    public Response execute(Request request) {
        logger.debug("requestFile={}", contentFile);

        Response response = Response.file(contentFile);
        if (contentType != null) response.contentType(contentType);
        if (cacheHeader != null) response.header(HTTPHeaders.CACHE_CONTROL, cacheHeader);
        return response;
    }

    @Override
    public void cache(Duration maxAge) {
        cacheHeader = "public, max-age=" + maxAge.getSeconds();
    }
}
