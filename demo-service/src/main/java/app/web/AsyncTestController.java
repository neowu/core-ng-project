package app.web;

import core.framework.api.concurrent.AsyncExecutor;
import core.framework.api.http.ContentTypes;
import core.framework.api.util.Threads;
import core.framework.api.web.Controller;
import core.framework.api.web.Request;
import core.framework.api.web.Response;

import javax.inject.Inject;
import java.time.Duration;
import java.util.concurrent.Future;

/**
 * @author neo
 */
public class AsyncTestController implements Controller {
    @Inject
    AsyncExecutor asyncExecutor;

    @Override
    public Response execute(Request request) throws Exception {
        Future<String> future1 = asyncExecutor.submit("task1", this::task1);
        Future<String> future2 = asyncExecutor.submit("task2", this::task2);
        return Response.text(future1.get() + "-" + future2.get(), ContentTypes.TEXT_PLAIN);
    }

    private String task1() {
        Threads.sleepRoughly(Duration.ofSeconds(5));
        return "result1";
    }

    private String task2() {
        return "result2";
    }
}
