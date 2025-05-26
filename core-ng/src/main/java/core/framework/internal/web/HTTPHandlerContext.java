package core.framework.internal.web;

import core.framework.internal.web.bean.RequestBeanReader;
import core.framework.internal.web.bean.ResponseBeanWriter;
import core.framework.internal.web.http.IPAccessControl;
import core.framework.internal.web.http.RateControl;
import core.framework.internal.web.request.RequestParser;

import javax.annotation.Nullable;

public class HTTPHandlerContext {
    public final RequestParser requestParser = new RequestParser();
    public final RequestBeanReader requestBeanReader = new RequestBeanReader();
    public final ResponseBeanWriter responseBeanWriter = new ResponseBeanWriter();
    @Nullable
    public RateControl rateControl;
    @Nullable
    public IPAccessControl accessControl;
}
