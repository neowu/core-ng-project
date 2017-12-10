package core.framework.impl.web.api;

import core.framework.impl.json.JSONMapper;
import core.framework.util.Lists;

import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public class OpenAPIManager {
    public final List<Class<?>> serviceInterfaces = Lists.newArrayList();
    private final String title;
    private byte[] document;

    public OpenAPIManager(String title) {
        this.title = title;
    }

    public byte[] document() {
        if (document == null) {
            document = JSONMapper.toJSON(build());
        }
        return document;
    }

    Map<String, Object> build() {
        OpenAPIDocumentBuilder builder = new OpenAPIDocumentBuilder();
        builder.title(title);
        serviceInterfaces.forEach(builder::addServiceInterface);
        return builder.document.properties;
    }
}
