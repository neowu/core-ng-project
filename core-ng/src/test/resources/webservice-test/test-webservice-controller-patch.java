public class TestWebServiceImpl$patch implements core.framework.web.Controller {
    private final core.framework.impl.web.service.TestWebService delegate;

    public TestWebServiceImpl$patch(core.framework.impl.web.service.TestWebService $1) {
        this.delegate = $1;
    }

    public core.framework.web.Response execute(core.framework.web.Request request) throws Exception {
        java.lang.Integer id = (java.lang.Integer) request.pathParam("id", java.lang.Integer.class);
        core.framework.impl.web.service.TestWebService.TestRequest bean = (core.framework.impl.web.service.TestWebService.TestRequest) request.bean(core.framework.impl.web.service.TestWebService.TestRequest.class);
        delegate.patch(id, bean);
        return core.framework.web.Response.empty().status(core.framework.api.http.HTTPStatus.OK);
    }

}
