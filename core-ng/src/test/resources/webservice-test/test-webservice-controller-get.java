public class TestWebServiceImpl$get implements core.framework.web.Controller {
    private final core.framework.impl.web.service.TestWebService delegate;

    public TestWebServiceImpl$get(core.framework.impl.web.service.TestWebService $1) {
        this.delegate = $1;
    }

    public core.framework.web.Response execute(core.framework.web.Request request) throws Exception {
        Integer $id = core.framework.impl.web.service.PathParamHelper.toInt(request.pathParam("id"));
        java.util.Optional response = delegate.get($id);
        return core.framework.web.Response.bean(response);
    }

}
