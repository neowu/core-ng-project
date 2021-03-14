package core.visualization.web;

import core.framework.http.ContentType;
import core.framework.util.Files;
import core.framework.web.Request;
import core.framework.web.Response;
import core.framework.web.site.WebDirectory;

/**
 * @author neo
 */
public class VisualizationController {
    private final byte[] html;

    public VisualizationController(WebDirectory directory) {
        html = Files.bytes(directory.path("/visualization.html"));
    }

    public Response home(Request request) {
        return Response.bytes(html).contentType(ContentType.TEXT_HTML);
    }
}
