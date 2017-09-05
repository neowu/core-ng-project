public class TestWebServiceImpl$create implements core.framework.api.web.Controller {
    private final core.framework.impl.web.service.TestWebService delegate;

    public TestWebServiceImpl$create(core.framework.impl.web.service.TestWebService $1) {
        this.delegate = $1;
    }

    public core.framework.api.web.Response execute(core.framework.api.web.Request request) throws Exception {
        java.lang.Integer id = (java.lang.Integer) request.pathParam("id", java.lang.Integer.class);
        core.framework.impl.web.service.TestWebService.TestRequest bean = (core.framework.impl.web.service.TestWebService.TestRequest) request.bean(core.framework.impl.web.service.TestWebService.TestRequest.class);
        delegate.create(id, bean);
        return core.framework.api.web.Response.empty().status(core.framework.api.http.HTTPStatus.CREATED);
    }

}
