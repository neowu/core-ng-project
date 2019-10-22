package core.framework.internal.web.site;

import core.framework.http.ContentType;
import core.framework.util.ASCII;
import core.framework.util.Maps;

import java.util.Map;

/**
 * @author neo
 */
final class MimeTypes {
    // refer to /etc/nginx/mime.types
    private static final Map<String, ContentType> MIME_TYPES = Maps.newHashMapWithExpectedSize(40);

    static {
        MIME_TYPES.put("html", ContentType.TEXT_HTML);
        MIME_TYPES.put("htm", ContentType.TEXT_HTML);
        MIME_TYPES.put("css", ContentType.TEXT_CSS);
        MIME_TYPES.put("xml", ContentType.TEXT_XML);
        MIME_TYPES.put("txt", ContentType.TEXT_PLAIN);

        MIME_TYPES.put("gif", ContentType.create("image/gif", null));
        MIME_TYPES.put("jpg", ContentType.create("image/jpeg", null));
        MIME_TYPES.put("jpeg", ContentType.create("image/jpeg", null));
        MIME_TYPES.put("png", ContentType.IMAGE_PNG);
        MIME_TYPES.put("tiff", ContentType.create("image/tiff", null));
        MIME_TYPES.put("tif", ContentType.create("image/tiff", null));
        MIME_TYPES.put("ico", ContentType.create("image/x-icon", null));

        MIME_TYPES.put("js", ContentType.APPLICATION_JAVASCRIPT);
        MIME_TYPES.put("json", ContentType.APPLICATION_JSON);

        MIME_TYPES.put("woff", ContentType.create("application/font-woff", null));
        MIME_TYPES.put("eot", ContentType.create("application/vnd.ms-fontobject", null));
        MIME_TYPES.put("ttf", ContentType.create("font/ttf", null));
        MIME_TYPES.put("otf", ContentType.create("font/opentype", null));

        MIME_TYPES.put("pdf", ContentType.create("application/pdf", null));
        MIME_TYPES.put("rtf", ContentType.create("application/rtf", null));
        MIME_TYPES.put("doc", ContentType.create("application/msword", null));
        MIME_TYPES.put("xls", ContentType.create("application/vnd.ms-excel", null));
        MIME_TYPES.put("ppt", ContentType.create("application/vnd.ms-powerpoint", null));
        MIME_TYPES.put("docx", ContentType.create("application/vnd.openxmlformats-officedocument.wordprocessingml.document", null));
        MIME_TYPES.put("xlsx", ContentType.create("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", null));
        MIME_TYPES.put("pptx", ContentType.create("application/vnd.openxmlformats-officedocument.presentationml.presentation", null));

        MIME_TYPES.put("swf", ContentType.create("application/x-shockwave-flash", null));
        MIME_TYPES.put("zip", ContentType.create("application/zip", null));
    }

    static ContentType get(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index > 0 && index + 1 < fileName.length()) {
            String extension = fileName.substring(index + 1);
            return MIME_TYPES.get(ASCII.toLowerCase(extension));
        }
        return null;
    }
}
