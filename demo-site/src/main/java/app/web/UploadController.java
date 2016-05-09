package app.web;

import core.framework.api.http.ContentType;
import core.framework.api.web.MultipartFile;
import core.framework.api.web.Request;
import core.framework.api.web.Response;

import javax.inject.Inject;

/**
 * @author neo
 */
public class UploadController {
    @Inject
    LanguageManager languageManager;

    public Response get(Request request) {
        return Response.html("/template/upload.html", new UploadPage(), languageManager.language());
    }

    public Response post(Request request) {
        MultipartFile file = request.file("test").get();
        return Response.text("uploaded, fileName=" + file.fileName, ContentType.TEXT_PLAIN);
    }
}
