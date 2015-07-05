package app.web;

import core.framework.api.util.ClasspathResources;
import core.framework.api.web.Controller;
import core.framework.api.web.Request;
import core.framework.api.web.Response;
import io.undertow.util.MimeMappings;

/**
 * @author neo
 */
public class AssetController implements Controller {
    final String prefix;

    public AssetController(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public Response execute(Request request) throws Exception {
        String asset = request.pathParam("asset");
        byte[] bytes = ClasspathResources.bytes(prefix + asset);
        int index = asset.lastIndexOf('.');
        String ext = asset.substring(index + 1);
        String contentType = MimeMappings.DEFAULT.getMimeType(ext);
        return Response.bytes(bytes, contentType);
    }
}
