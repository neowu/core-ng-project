package core.framework.impl.web.site;

import core.framework.api.util.Maps;
import core.framework.api.util.Strings;

import java.util.Map;

/**
 * @author neo
 */
final class MimeTypes {
    // refer to /etc/nginx/mime.types
    private static final Map<String, String> MIME_TYPES = Maps.newHashMapWithExpectedSize(50);

    static {
        MIME_TYPES.put("html", "text/html");
        MIME_TYPES.put("htm", "text/html");
        MIME_TYPES.put("css", "text/css");
        MIME_TYPES.put("xml", "text/xml");
        MIME_TYPES.put("txt", "text/plain");

        MIME_TYPES.put("gif", "image/gif");
        MIME_TYPES.put("jpg", "image/jpeg");
        MIME_TYPES.put("jpeg", "image/jpeg");
        MIME_TYPES.put("png", "image/png");
        MIME_TYPES.put("tiff", "image/tiff");
        MIME_TYPES.put("tif", "image/tiff");
        MIME_TYPES.put("ico", "image/x-icon");

        MIME_TYPES.put("js", "application/javascript");
        MIME_TYPES.put("json", "application/json");

        MIME_TYPES.put("woff", "application/font-woff");
        MIME_TYPES.put("eot", "application/vnd.ms-fontobject");
        MIME_TYPES.put("ttf", "font/ttf");
        MIME_TYPES.put("otf", "font/opentype");

        MIME_TYPES.put("pdf", "application/pdf");
        MIME_TYPES.put("rtf", "application/rtf");
        MIME_TYPES.put("doc", "application/msword");
        MIME_TYPES.put("xls", "application/vnd.ms-excel");
        MIME_TYPES.put("ppt", "application/vnd.ms-powerpoint");
        MIME_TYPES.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        MIME_TYPES.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        MIME_TYPES.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");

        MIME_TYPES.put("swf", "application/x-shockwave-flash");
        MIME_TYPES.put("zip", "application/zip");
    }

    static String get(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index > 0 && index + 1 < fileName.length()) {
            String extension = fileName.substring(index + 1);
            return MIME_TYPES.get(Strings.toLowerCase(extension));
        }
        return null;
    }
}
