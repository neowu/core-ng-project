package core.framework.impl.web.site;

import core.framework.api.http.ContentType;
import core.framework.api.util.Maps;
import core.framework.api.util.Strings;

import java.util.Map;

/**
 * @author neo
 */
final class MimeTypes {
    // refer to /etc/nginx/mime.types
    private static final Map<String, ContentType> MIME_TYPES = Maps.newHashMapWithExpectedSize(50);

    static {
        MIME_TYPES.put("html", ContentType.TEXT_HTML);
        MIME_TYPES.put("htm", ContentType.TEXT_HTML);
        MIME_TYPES.put("css", ContentType.TEXT_CSS);
        MIME_TYPES.put("xml", ContentType.TEXT_XML);
        MIME_TYPES.put("txt", ContentType.TEXT_PLAIN);

        MIME_TYPES.put("gif", new ContentType("image/gif", null));
        MIME_TYPES.put("jpg", new ContentType("image/jpeg", null));
        MIME_TYPES.put("jpeg", new ContentType("image/jpeg", null));
        MIME_TYPES.put("png", new ContentType("image/png", null));
        MIME_TYPES.put("tiff", new ContentType("image/tiff", null));
        MIME_TYPES.put("tif", new ContentType("image/tiff", null));
        MIME_TYPES.put("ico", new ContentType("image/x-icon", null));

        MIME_TYPES.put("js", ContentType.APPLICATION_JAVASCRIPT);
        MIME_TYPES.put("json", ContentType.APPLICATION_JSON);

        MIME_TYPES.put("woff", new ContentType("application/font-woff", null));
        MIME_TYPES.put("eot", new ContentType("application/vnd.ms-fontobject", null));
        MIME_TYPES.put("ttf", new ContentType("font/ttf", null));
        MIME_TYPES.put("otf", new ContentType("font/opentype", null));

        MIME_TYPES.put("pdf", new ContentType("application/pdf", null));
        MIME_TYPES.put("rtf", new ContentType("application/rtf", null));
        MIME_TYPES.put("doc", new ContentType("application/msword", null));
        MIME_TYPES.put("xls", new ContentType("application/vnd.ms-excel", null));
        MIME_TYPES.put("ppt", new ContentType("application/vnd.ms-powerpoint", null));
        MIME_TYPES.put("docx", new ContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document", null));
        MIME_TYPES.put("xlsx", new ContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", null));
        MIME_TYPES.put("pptx", new ContentType("application/vnd.openxmlformats-officedocument.presentationml.presentation", null));

        MIME_TYPES.put("swf", new ContentType("application/x-shockwave-flash", null));
        MIME_TYPES.put("zip", new ContentType("application/zip", null));
    }

    static ContentType get(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index > 0 && index + 1 < fileName.length()) {
            String extension = fileName.substring(index + 1);
            return MIME_TYPES.get(Strings.toLowerCase(extension));
        }
        return null;
    }
}
