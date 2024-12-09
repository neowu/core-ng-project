package core.log.web;

import core.framework.async.Executor;
import core.framework.inject.Inject;
import core.framework.web.Controller;
import core.framework.web.Request;
import core.framework.web.Response;
import core.log.service.ArchiveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author neo
 */
public class UploadController implements Controller {
    private final Logger logger = LoggerFactory.getLogger(UploadController.class);

    @Inject
    ArchiveService archiveService;
    @Inject
    Executor executor;

    @Override
    public Response execute(Request request) {
        UploadRequest uploadRequest = request.bean(UploadRequest.class);
        logger.info("manually upload, date={}", uploadRequest.date);
        executor.submit("upload", () -> archiveService.uploadArchive(uploadRequest.date));
        return Response.empty();
    }
}
