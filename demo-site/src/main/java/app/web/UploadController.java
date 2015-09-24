package app.web;

import core.framework.api.http.ContentTypes;
import core.framework.api.web.MultipartFile;
import core.framework.api.web.Request;
import core.framework.api.web.Response;

/**
 * @author neo
 */
public class UploadController {
    public Response get(Request request) {
        return Response.html("/template/upload.html", new UploadPage());
    }

    public Response post(Request request) {
        MultipartFile file = request.file("test").get();
        return Response.text("uploaded, fileName=" + file.fileName, ContentTypes.TEXT_PLAIN);
    }
}
