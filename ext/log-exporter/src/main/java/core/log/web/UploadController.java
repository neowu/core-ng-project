package core.log.web;

import core.framework.inject.Inject;
import core.framework.web.Controller;
import core.framework.web.Request;
import core.framework.web.Response;
import core.log.service.ArchiveService;

/**
 * @author neo
 */
public class UploadController implements Controller {
    @Inject
    ArchiveService archiveService;

    @Override
    public Response execute(Request request) {
        UploadRequest uploadRequest = request.bean(UploadRequest.class);
        archiveService.uploadArchive(uploadRequest.date);
        return Response.empty();
    }
}
