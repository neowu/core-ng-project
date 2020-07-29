package core.framework.internal.web.response;

import core.framework.internal.web.bean.ResponseBeanWriter;
import core.framework.internal.web.site.TemplateManager;

/**
 * @author neo
 */
final class ResponseHandlerContext {
    final ResponseBeanWriter writer;
    final TemplateManager templateManager;

    ResponseHandlerContext(ResponseBeanWriter writer, TemplateManager templateManager) {
        this.writer = writer;
        this.templateManager = templateManager;
    }
}
