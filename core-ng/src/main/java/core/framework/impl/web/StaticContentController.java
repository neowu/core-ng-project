package core.framework.impl.web;

import core.framework.api.http.HTTPHeaders;
import core.framework.api.http.HTTPStatus;
import core.framework.api.util.Exceptions;
import core.framework.api.web.Controller;
import core.framework.api.web.Request;
import core.framework.api.web.Response;
import core.framework.api.web.exception.NotFoundException;
import io.undertow.util.DateUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Date;
import java.util.Optional;

/**
 * @author neo
 */
public class StaticContentController implements Controller {
    private final Path contentDirectory;

    public StaticContentController(WebDirectory webDirectory, String root) {
        contentDirectory = webDirectory.path(root);
        if (!Files.isDirectory(contentDirectory))
            throw Exceptions.error("content root must be directory, root={}", contentDirectory);
    }

    @Override
    public Response execute(Request request) throws Exception {
        String path = request.pathParam("path");
        Path absolutePath = contentDirectory.resolve(path);

        if (!Files.isRegularFile(absolutePath, LinkOption.NOFOLLOW_LINKS))
            throw new NotFoundException("not found, path=" + request.path());

        File file = absolutePath.toFile();
        Optional<String> ifModifiedHeader = request.header(HTTPHeaders.IF_MODIFIED_SINCE);
        if (ifModifiedHeader.isPresent() && !DateUtils.handleIfModifiedSince(ifModifiedHeader.get(), new Date(file.lastModified()))) {
            return Response.empty().status(HTTPStatus.NOT_MODIFIED);
        }

        return Response.file(file);
    }
}
