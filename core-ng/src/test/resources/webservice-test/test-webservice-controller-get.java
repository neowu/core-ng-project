public class Controller$TestWebServiceImpl$get implements core.framework.web.Controller {
    private final core.framework.internal.web.service.TestWebService delegate;

    public Controller$TestWebServiceImpl$get(core.framework.internal.web.service.TestWebService $1) {
        this.delegate = $1;
    }

    public core.framework.web.Response execute(core.framework.web.Request request) throws Exception {
        Integer $id = core.framework.internal.web.service.PathParamHelper.toInt(request.pathParam("id"));
        java.util.Optional response = delegate.get($id);
        return core.framework.web.Response.bean(response);
    }

}
