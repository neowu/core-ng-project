package core.visualization.web;

import core.framework.http.ContentType;
import core.framework.inject.Inject;
import core.framework.util.Files;
import core.framework.web.Request;
import core.framework.web.Response;
import core.framework.web.site.WebDirectory;

/**
 * @author neo
 */
public class DiagramController {
    @Inject
    WebDirectory directory;

    public Response home(Request request) {
        byte[] html = Files.bytes(directory.path("/diagram.html"));
        return Response.bytes(html).contentType(ContentType.TEXT_HTML);
    }
}
