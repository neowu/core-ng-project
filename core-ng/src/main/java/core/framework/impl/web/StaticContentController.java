package core.framework.impl.web;

import core.framework.api.http.HTTPHeaders;
import core.framework.api.http.HTTPStatus;
import core.framework.api.util.Exceptions;
import core.framework.api.web.Controller;
import core.framework.api.web.Request;
import core.framework.api.web.Response;
import core.framework.api.web.exception.NotFoundException;
import io.undertow.util.DateUtils;
import io.undertow.util.MimeMappings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;

import static core.framework.api.util.Files.lastModified;

/**
 * @author neo
 */
public class StaticContentController implements Controller {
    private final Logger logger = LoggerFactory.getLogger(StaticContentController.class);
    private final Path contentDirectory;

    public StaticContentController(WebDirectory webDirectory, String root) {
        contentDirectory = webDirectory.path(root);
        if (!Files.isDirectory(contentDirectory))
            throw Exceptions.error("content root must be directory, root={}", contentDirectory);
    }

    @Override
    public Response execute(Request request) throws Exception {
        String path = request.pathParam("path");
        Path absolutePath = contentDirectory.resolve(path).toAbsolutePath();
        logger.debug("requestFile={}", absolutePath);

        if (!Files.isRegularFile(absolutePath, LinkOption.NOFOLLOW_LINKS))
            throw new NotFoundException("not found, path=" + request.path());

        File file = absolutePath.toFile();
        Optional<String> ifModifiedHeader = request.header(HTTPHeaders.IF_MODIFIED_SINCE);
        if (ifModifiedHeader.isPresent() && !DateUtils.handleIfModifiedSince(ifModifiedHeader.get(), new Date(file.lastModified()))) {
            return Response.empty().status(HTTPStatus.NOT_MODIFIED);
        }

        Response response = Response.file(file);
        String fileName = file.getName();
        int index = fileName.lastIndexOf('.');
        if (index > 0 && index + 1 < fileName.length()) {
            String extension = fileName.substring(index + 1);
            String contentType = MimeMappings.DEFAULT.getMimeType(extension);
            response.header(HTTPHeaders.CONTENT_TYPE, contentType);
        }
        response.header(HTTPHeaders.LAST_MODIFIED, DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.ofInstant(lastModified(absolutePath), ZoneId.of("GMT"))));
        return response;
    }
}
